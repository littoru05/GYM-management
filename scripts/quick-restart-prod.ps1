#Requires -Version 5.1
<#
.SYNOPSIS
    Emergency Quick Restart & Zero-Downtime Recovery Runbook Script.

.DESCRIPTION
    Executes fast high-availability emergency recovery:
      1. Automatic RDS MySQL status check and wake (start-db-instance & wait available)
      2. Zero-downtime rolling task force-update on ECS Fargate (backend & frontend)
      3. Dynamic DNS DuckDNS re-sync against active ALB IPv4 address if changed

.PARAMETER Environment
    Target environment: prod | staging (default: prod).

.PARAMETER Region
    AWS region (default: ap-southeast-1).

.PARAMETER DuckDnsDomain
    DuckDNS subdomain (default: gym-management or $env:DUCKDNS_DOMAIN).

.PARAMETER DuckDnsToken
    DuckDNS API token (default: $env:DUCKDNS_TOKEN).

.PARAMETER DryRun
    Simulate actions without executing write commands.

.PARAMETER SkipRds
    Skip RDS check and wake step.

.PARAMETER SkipDns
    Skip DuckDNS Dynamic DNS synchronization.

.PARAMETER Help
    Show usage information.

.EXAMPLE
    .\scripts\quick-restart-prod.ps1 -DryRun
    .\scripts\quick-restart-prod.ps1 -Environment "prod"
    .\scripts\quick-restart-prod.ps1 -SkipRds
#>
[CmdletBinding()]
param(
    [Alias("e", "Env")]
    [string]$Environment = "prod",

    [Alias("r")]
    [string]$Region = "ap-southeast-1",

    [string]$DuckDnsDomain = $(if ($env:DUCKDNS_PROD_DOMAIN) { $env:DUCKDNS_PROD_DOMAIN } elseif ($env:DUCKDNS_DOMAIN) { $env:DUCKDNS_DOMAIN } else { "gym-management" }),

    [string]$DuckDnsToken = $(if ($env:DUCKDNS_TOKEN) { $env:DUCKDNS_TOKEN } else { "" }),

    [switch]$DryRun,
    [switch]$SkipRds,
    [switch]$SkipDns,

    [Alias("h", "?")]
    [switch]$Help
)

$ErrorActionPreference = "Continue"

if ($Help -or $args -contains "-h" -or $args -contains "--help" -or $args -contains "-?") {
    Write-Host @"
Usage: .\scripts\quick-restart-prod.ps1 [options]

Options:
  -Environment, -Env, -e <string>  Target environment: prod | staging (default: prod)
  -Region, -r <string>             AWS region (default: ap-southeast-1)
  -DryRun                          Simulate actions without executing write commands
  -SkipRds                         Skip RDS check and wake step
  -SkipDns                         Skip DuckDNS Dynamic DNS synchronization
  -DuckDnsDomain <string>          DuckDNS subdomain (default: gym-management)
  -DuckDnsToken <string>           DuckDNS API token
  -Help, -h                        Show this help message and exit

Examples:
  .\scripts\quick-restart-prod.ps1 -DryRun
  .\scripts\quick-restart-prod.ps1 -Environment "prod"
  .\scripts\quick-restart-prod.ps1 -SkipRds
"@
    exit 0
}

# Clean DuckDNS domain
if ($DuckDnsDomain.EndsWith(".duckdns.org")) {
    $DuckDnsDomain = $DuckDnsDomain.Substring(0, $DuckDnsDomain.Length - 12)
}

$rdsIdentifier = "gym-$Environment-mysql"
$ecsCluster = "gym-$Environment-cluster"
$backendService = "gym-$Environment-backend"
$frontendService = "gym-$Environment-frontend"
$albName = "gym-$Environment-alb"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "   GYM MANAGEMENT - EMERGENCY QUICK RESTART & RECOVERY RUNBOOK   " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Environment : $Environment"
Write-Host "AWS Region  : $Region"
Write-Host "ECS Cluster : $ecsCluster"
Write-Host "RDS DB ID   : $rdsIdentifier"
Write-Host "Dry Run     : $(if ($DryRun) { 'YES (Simulated)' } else { 'NO (Live Execution)' })"
Write-Host "----------------------------------------------------------------------`n" -ForegroundColor Cyan

$startTime = [System.DateTime]::UtcNow

# ==============================================================================
# STEP 1: CHECK & WAKE RDS MYSQL DATABASE
# ==============================================================================
Write-Host "[STEP 1/3] Checking RDS MySQL Instance State..." -ForegroundColor White
if ($SkipRds) {
    Write-Host "  [SKIPPED] RDS check bypassed via -SkipRds flag.`n" -ForegroundColor Yellow
} else {
    if ($DryRun) {
        Write-Host "  [DRY-RUN] Would query RDS status for [$rdsIdentifier]"
        Write-Host "  [DRY-RUN] If stopped, would execute: aws rds start-db-instance --db-instance-identifier $rdsIdentifier"
        Write-Host "  [DRY-RUN] Would wait for status: available`n"
    } else {
        $rdsStatus = & aws rds describe-db-instances `
            --db-instance-identifier $rdsIdentifier `
            --region $Region `
            --query "DBInstances[0].DBInstanceStatus" `
            --output text 2>$null

        if (-not $rdsStatus -or $LASTEXITCODE -ne 0) {
            $rdsStatus = "NOT_FOUND"
        }

        Write-Host "  Current RDS Status: $rdsStatus"

        if ($rdsStatus -eq "available") {
            Write-Host "  [OK] RDS instance is already available and ready.`n" -ForegroundColor Green
        } elseif ($rdsStatus -eq "stopped" -or $rdsStatus -eq "starting") {
            if ($rdsStatus -eq "stopped") {
                Write-Host "  [ACTION] RDS instance is stopped. Triggering start-db-instance..." -ForegroundColor Yellow
                & aws rds start-db-instance `
                    --db-instance-identifier $rdsIdentifier `
                    --region $Region | Out-Null
            }

            Write-Host "  Waiting for RDS instance to become available..."
            & aws rds wait db-instance-available `
                --db-instance-identifier $rdsIdentifier `
                --region $Region
            Write-Host "  [OK] RDS instance is now available!`n" -ForegroundColor Green
        } elseif ($rdsStatus -eq "NOT_FOUND") {
            Write-Host "  [WARN] RDS instance [$rdsIdentifier] not found or access denied. Skipping RDS wake.`n" -ForegroundColor Yellow
        } else {
            Write-Host "  [INFO] RDS instance state: $rdsStatus. Proceeding with rolling deployment.`n" -ForegroundColor Yellow
        }
    }
}

# ==============================================================================
# STEP 2: ROLLING FORCE-NEW-DEPLOYMENT ON ECS FARGATE
# ==============================================================================
Write-Host "[STEP 2/3] Triggering Zero-Downtime Rolling Update on ECS Services..." -ForegroundColor White
foreach ($svc in @($backendService, $frontendService)) {
    if ($DryRun) {
        Write-Host "  [DRY-RUN] Would trigger rolling update: aws ecs update-service --cluster $ecsCluster --service $svc --force-new-deployment"
    } else {
        Write-Host "  Executing force update on [$svc]..."
        $updateRes = & aws ecs update-service `
            --cluster $ecsCluster `
            --service $svc `
            --force-new-deployment `
            --region $Region `
            --query "service.[serviceName,status,desiredCount,runningCount]" `
            --output text 2>$null

        if ($LASTEXITCODE -eq 0 -and $updateRes) {
            Write-Host "  [OK] Force update initiated successfully for $svc." -ForegroundColor Green
        } else {
            Write-Host "  [FAIL] Failed to update service $svc. Please check IAM permissions." -ForegroundColor Red
        }
    }
}
Write-Host ""

# ==============================================================================
# STEP 3: DUCKDNS DYNAMIC DNS SYNCHRONIZATION (WITH CHANGE DETECTION)
# ==============================================================================
Write-Host "[STEP 3/3] Dynamic DNS Re-sync (DuckDNS)..." -ForegroundColor White
if ($SkipDns) {
    Write-Host "  [SKIPPED] DuckDNS sync bypassed via -SkipDns flag.`n" -ForegroundColor Yellow
} else {
    if ($DryRun) {
        Write-Host "  [DRY-RUN] Would resolve ALB DNS for [$albName]"
        Write-Host "  [DRY-RUN] Would check if current DuckDNS IP differs from ALB IP"
        Write-Host "  [DRY-RUN] If changed, would call DuckDNS update API: https://www.duckdns.org/update?domains=$DuckDnsDomain&token=***&ip=<ALB_IP>`n"
    } else {
        $albDns = & aws elbv2 describe-load-balancers `
            --names $albName `
            --region $Region `
            --query "LoadBalancers[0].DNSName" `
            --output text 2>$null

        if ($LASTEXITCODE -eq 0 -and $albDns -and $albDns -ne "None") {
            Write-Host "  Found ALB DNS: $albDns"
            $albIp = $null
            for ($i = 1; $i -le 5; $i++) {
                try {
                    $ips = [System.Net.Dns]::GetHostAddresses($albDns) | Where-Object { $_.AddressFamily -eq 'InterNetwork' }
                    if ($ips -and $ips.Count -gt 0) {
                        $albIp = $ips[0].IPAddressToString
                        break
                    }
                } catch {
                    # Ignore and retry
                }
                Start-Sleep -Seconds 2
            }

            if ($albIp) {
                Write-Host "  Resolved ALB Public IPv4: $albIp"

                # Check current DuckDNS IP to detect change
                $currentDuckIp = $null
                try {
                    $duckIps = [System.Net.Dns]::GetHostAddresses("$DuckDnsDomain.duckdns.org") | Where-Object { $_.AddressFamily -eq 'InterNetwork' }
                    if ($duckIps -and $duckIps.Count -gt 0) {
                        $currentDuckIp = $duckIps[0].IPAddressToString
                    }
                } catch {
                    # Domain not resolvable yet
                }

                if ($currentDuckIp -and $currentDuckIp -eq $albIp) {
                    Write-Host "  [OK] DuckDNS IP ($currentDuckIp) is already synchronized with ALB IP. No update needed." -ForegroundColor Green
                } else {
                    Write-Host "  DuckDNS IP change detected (Current: $(if ($currentDuckIp) { $currentDuckIp } else { 'Unresolved' }) -> ALB: $albIp). Re-syncing..."
                    if ($DuckDnsToken) {
                        Write-Host "  Synchronizing DuckDNS ($DuckDnsDomain.duckdns.org -> $albIp)..."
                        try {
                            $ddnsUrl = "https://www.duckdns.org/update?domains=$DuckDnsDomain&token=$DuckDnsToken&ip=$albIp"
                            $resp = (New-Object System.Net.WebClient).DownloadString($ddnsUrl)
                            if ($resp.Trim() -eq "OK") {
                                Write-Host "  [OK] DuckDNS synchronized successfully (OK)." -ForegroundColor Green
                            } else {
                                Write-Host "  [WARN] DuckDNS API returned: $resp" -ForegroundColor Yellow
                            }
                        } catch {
                            Write-Host "  [WARN] Failed to call DuckDNS update API: $_" -ForegroundColor Yellow
                        }
                    } else {
                        Write-Host "  [INFO] DuckDnsToken not provided. Skipping DuckDNS API call." -ForegroundColor Yellow
                        Write-Host "    Manual sync URL: https://www.duckdns.org/update?domains=$DuckDnsDomain&token=<TOKEN>&ip=$albIp"
                    }
                }
            } else {
                Write-Host "  [WARN] Could not resolve IPv4 address for ALB DNS [$albDns]." -ForegroundColor Yellow
            }
        } else {
            Write-Host "  [WARN] ALB [$albName] not found or access denied." -ForegroundColor Yellow
        }
    }
    Write-Host ""
}

$endTime = [System.DateTime]::UtcNow
$elapsedSeconds = [int](($endTime - $startTime).TotalSeconds)

Write-Host "======================================================================" -ForegroundColor Cyan
if ($elapsedSeconds -le 60) {
    Write-Host "   EMERGENCY RESTART DISPATCH COMPLETED IN ${elapsedSeconds}s (WITHIN 60s TARGET)   " -ForegroundColor Green
} else {
    Write-Host "   EMERGENCY RESTART COMPLETED IN ${elapsedSeconds}s (> 60s DUE TO RDS WAKE TIME)    " -ForegroundColor Yellow
}
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Next steps:"
Write-Host "  1. Run multi-tier health check: .\scripts\check-ha-health.ps1 -HttpOnly"
Write-Host "  2. Tail backend CloudWatch logs: aws logs tail /ecs/gym-$Environment-backend --follow"
Write-Host "  3. Open Live URL: http://$DuckDnsDomain.duckdns.org`n"
exit 0
