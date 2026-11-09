#Requires -Version 5.1
<#
.SYNOPSIS
    Multi-Tier High Availability Health Check for GYM Management System.

.DESCRIPTION
    Performs independent health checks across three tiers:
      Tier 1: AWS Infrastructure (ALB Target Group Health & ECS Services/Tasks)
      Tier 2: Deep Backend Health (/actuator/health with MySQL & diskSpace)
      Tier 3: Frontend Web SPA (Root HTTP 200 & /version.json Artifact Version)

.PARAMETER BaseUrl
    Base URL to test (default: http://gym-management.duckdns.org or $env:APP_URL).

.PARAMETER Environment
    Target environment: prod | staging (default: prod).

.PARAMETER Region
    AWS region (default: ap-southeast-1).

.PARAMETER TimeoutSeconds
    HTTP request timeout in seconds (default: 5).

.PARAMETER HttpOnly
    Skip Tier 1 AWS CLI checks and only test Tier 2 & Tier 3 via HTTP.

.PARAMETER Help
    Show usage information.

.EXAMPLE
    .\scripts\check-ha-health.ps1 -HttpOnly
    .\scripts\check-ha-health.ps1 -BaseUrl "http://gym-management.duckdns.org"
    .\scripts\check-ha-health.ps1 -Environment "prod" -Region "ap-southeast-1"
#>
[CmdletBinding()]
param(
    [Alias("u", "Url")]
    [string]$BaseUrl = $(if ($env:APP_URL) { $env:APP_URL } else { "http://gym-management.duckdns.org" }),

    [Alias("e", "Env")]
    [string]$Environment = "prod",

    [Alias("r")]
    [string]$Region = "ap-southeast-1",

    [Alias("t")]
    [int]$TimeoutSeconds = 5,

    [switch]$HttpOnly,

    [Alias("h", "?")]
    [switch]$Help
)

$ErrorActionPreference = "Continue"

if ($Help -or $args -contains "-h" -or $args -contains "--help" -or $args -contains "-?") {
    Write-Host @"
Usage: .\scripts\check-ha-health.ps1 [options]

Options:
  -BaseUrl, -Url, -u <string>     Base URL to test (default: http://gym-management.duckdns.org)
  -Environment, -Env, -e <string> Target environment: prod | staging (default: prod)
  -Region, -r <string>            AWS region (default: ap-southeast-1)
  -TimeoutSeconds, -t <int>       HTTP timeout in seconds (default: 5)
  -HttpOnly                       Skip Tier 1 AWS CLI checks and only test Tier 2 & Tier 3 via HTTP
  -Help, -h                       Show this help message and exit

Examples:
  .\scripts\check-ha-health.ps1 -HttpOnly
  .\scripts\check-ha-health.ps1 -BaseUrl "http://gym-management.duckdns.org"
"@
    exit 0
}

# Helper function to avoid duplicated HttpWebRequest code
function Invoke-HttpProbe {
    param(
        [string]$ProbeUrl,
        [int]$TimeoutSec
    )
    $statusCode = 0
    $body = $null
    try {
        $req = [System.Net.HttpWebRequest]::Create($ProbeUrl)
        $req.Timeout = $TimeoutSec * 1000
        $req.Method = "GET"
        $resp = $req.GetResponse()
        $statusCode = [int]$resp.StatusCode
        $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
        $body = $reader.ReadToEnd()
        $reader.Close()
        $resp.Close()
    } catch [System.Net.WebException] {
        if ($_.Response) {
            $statusCode = [int]$_.Response.StatusCode
            $reader = New-Object System.IO.StreamReader($_.Response.GetResponseStream())
            $body = $reader.ReadToEnd()
            $reader.Close()
        }
    } catch {
        $statusCode = 0
    }
    return [PSCustomObject]@{
        StatusCode = $statusCode
        Body       = $body
    }
}

# Trim trailing slash
$BaseUrl = $BaseUrl.TrimEnd('/')

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "   GYM MANAGEMENT - MULTI-TIER HIGH AVAILABILITY HEALTH MONITOR   " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Environment : $Environment"
Write-Host "AWS Region  : $Region"
Write-Host "Target URL  : $BaseUrl"
Write-Host "Mode        : $(if ($HttpOnly) { 'HTTP-Only (Tier 2 & 3)' } else { 'Full Multi-Tier (Tier 1, 2, 3)' })"
Write-Host "Timeout     : ${TimeoutSeconds}s"
Write-Host "----------------------------------------------------------------------`n" -ForegroundColor Cyan

$overallPass = $true

# ==============================================================================
# TIER 1: AWS INFRASTRUCTURE (ALB Target Groups & ECS Services/Tasks)
# ==============================================================================
if ($HttpOnly) {
    Write-Host "[TIER 1 SKIPPED] AWS Infrastructure check bypassed via -HttpOnly flag.`n" -ForegroundColor Yellow
} else {
    Write-Host "[TIER 1] AWS Infrastructure Health (ALB Target Group & ECS Services)..." -ForegroundColor White
    $awsCli = Get-Command "aws" -ErrorAction SilentlyContinue

    if (-not $awsCli) {
        Write-Host "  [FAIL] AWS CLI not found in PATH. Install AWS CLI or run with -HttpOnly." -ForegroundColor Red
        $overallPass = $false
    } else {
        $clusterName = "gym-$Environment-cluster"
        $backendTgName = "gym-$Environment-backend-tg"
        $frontendTgName = "gym-$Environment-frontend-tg"
        $backendService = "gym-$Environment-backend"
        $frontendService = "gym-$Environment-frontend"

        # Check ALB Target Groups
        Write-Host "  Checking ALB Target Groups..."
        foreach ($tgName in @($backendTgName, $frontendTgName)) {
            $tgArn = & aws elbv2 describe-target-groups `
                --names $tgName `
                --region $Region `
                --query "TargetGroups[0].TargetGroupArn" `
                --output text 2>$null

            if ($LASTEXITCODE -eq 0 -and $tgArn -and $tgArn -ne "None") {
                $healthJsonRaw = & aws elbv2 describe-target-health `
                    --target-group-arn $tgArn `
                    --region $Region `
                    --output json 2>$null

                if ($LASTEXITCODE -eq 0 -and $healthJsonRaw) {
                    $healthData = $healthJsonRaw | ConvertFrom-Json
                    $totalTargets = $healthData.TargetHealthDescriptions.Count
                    $healthyCount = ($healthData.TargetHealthDescriptions | Where-Object { $_.TargetHealth.State -eq "healthy" }).Count
                    $unhealthyCount = ($healthData.TargetHealthDescriptions | Where-Object { $_.TargetHealth.State -eq "unhealthy" }).Count

                    if ($healthyCount -gt 0 -and $unhealthyCount -eq 0) {
                        Write-Host "    [OK] Target Group [$tgName]: Healthy ($healthyCount/$totalTargets targets healthy)" -ForegroundColor Green
                    } else {
                        Write-Host "    [FAIL] Target Group [$tgName]: Degraded (Healthy: $healthyCount, Unhealthy: $unhealthyCount, Total: $totalTargets)" -ForegroundColor Red
                        $overallPass = $false
                    }
                } else {
                    Write-Host "    [WARN] Target Group [$tgName]: Could not describe target health." -ForegroundColor Yellow
                    $overallPass = $false
                }
            } else {
                Write-Host "    [WARN] Target Group [$tgName]: Not found or access denied in region $Region." -ForegroundColor Yellow
                $overallPass = $false
            }
        }

        # Check ECS Services & Tasks
        Write-Host "  Checking ECS Services and Tasks in cluster [$clusterName]..."
        $servicesJsonRaw = & aws ecs describe-services `
            --cluster $clusterName `
            --services $backendService $frontendService `
            --region $Region `
            --output json 2>$null

        if ($LASTEXITCODE -eq 0 -and $servicesJsonRaw) {
            $servicesData = $servicesJsonRaw | ConvertFrom-Json
            foreach ($svcName in @($backendService, $frontendService)) {
                $svc = $servicesData.services | Where-Object { $_.serviceName -eq $svcName }
                if ($svc -and $svc.desiredCount -gt 0 -and $svc.runningCount -ge $svc.desiredCount) {
                    Write-Host "    [OK] ECS Service [$svcName]: Running ($($svc.runningCount)/$($svc.desiredCount) tasks running)" -ForegroundColor Green
                } else {
                    $runningCount = if ($svc) { $svc.runningCount } else { 0 }
                    $desiredCount = if ($svc) { $svc.desiredCount } else { 0 }
                    Write-Host "    [FAIL] ECS Service [$svcName]: Degraded ($runningCount/$desiredCount tasks running)" -ForegroundColor Red
                    $overallPass = $false
                }
            }

            # Query individual active ECS tasks state
            $taskArns = & aws ecs list-tasks --cluster $clusterName --region $Region --query "taskArns[]" --output text 2>$null
            if ($taskArns -and $taskArns -ne "None") {
                $count = ($taskArns -split "`t| " | Where-Object { $_ -ne "" }).Count
                Write-Host "    [OK] Active ECS Tasks in cluster [$clusterName]: $count task(s) active" -ForegroundColor Green
            }
        } else {
            Write-Host "    [WARN] ECS Cluster or services could not be described." -ForegroundColor Yellow
            $overallPass = $false
        }
    }
    Write-Host ""
}

# ==============================================================================
# TIER 2: DEEP BACKEND HEALTH (/actuator/health)
# ==============================================================================
Write-Host "[TIER 2] Deep Backend Actuator Health Probe..." -ForegroundColor White
$actuatorUrl = "$BaseUrl/actuator/health"
Write-Host "  Probing: $actuatorUrl"

$actuatorResult = Invoke-HttpProbe -ProbeUrl $actuatorUrl -TimeoutSec $TimeoutSeconds

if ($actuatorResult.StatusCode -eq 200 -and $actuatorResult.Body) {
    try {
        $json = $actuatorResult.Body | ConvertFrom-Json
        $statusVal = $json.status

        # Check deep components (db & diskSpace)
        $hasDbUp = $false
        $hasDiskUp = $false

        if ($json.components) {
            if ($json.components.db -and $json.components.db.status -eq "UP") { $hasDbUp = $true }
            if ($json.components.diskSpace -and $json.components.diskSpace.status -eq "UP") { $hasDiskUp = $true }
        }

        # Fallback regex checks for string match if nested parsing varies
        if (-not $hasDbUp -and ($actuatorResult.Body -match '"db"\s*:\s*\{[^}]*"status"\s*:\s*"UP"')) { $hasDbUp = $true }
        if (-not $hasDiskUp -and ($actuatorResult.Body -match '"diskSpace"\s*:\s*\{[^}]*"status"\s*:\s*"UP"')) { $hasDiskUp = $true }

        if ($statusVal -eq "UP") {
            Write-Host "  [OK] Backend Actuator Status : UP (HTTP 200)" -ForegroundColor Green
            if ($hasDbUp) {
                Write-Host "  [OK] MySQL Database Component : UP (HikariCP pool active)" -ForegroundColor Green
            } else {
                Write-Host "  [FAIL] MySQL Database Component : Component status not UP" -ForegroundColor Red
                $overallPass = $false
            }

            if ($hasDiskUp) {
                Write-Host "  [OK] Disk Space Component     : UP (Free space threshold ok)" -ForegroundColor Green
            } else {
                Write-Host "  [FAIL] Disk Space Component     : Component status not UP" -ForegroundColor Red
                $overallPass = $false
            }
        } else {
            Write-Host "  [FAIL] Backend Actuator Status : $statusVal (Expected: UP)" -ForegroundColor Red
            $overallPass = $false
        }
    } catch {
        Write-Host "  [FAIL] Backend Actuator response is not valid JSON: $_" -ForegroundColor Red
        $overallPass = $false
    }
} else {
    Write-Host "  [FAIL] Backend Actuator Probe Failed : HTTP Code $($actuatorResult.StatusCode)" -ForegroundColor Red
    $overallPass = $false
}
Write-Host ""

# ==============================================================================
# TIER 3: FRONTEND WEB (Root / and /version.json)
# ==============================================================================
Write-Host "[TIER 3] Frontend Web Readiness & Version Probe..." -ForegroundColor White
$rootUrl = "$BaseUrl/"
$versionUrl = "$BaseUrl/version.json"

# Probe Root SPA
Write-Host "  Probing Root SPA: $rootUrl"
$rootResult = Invoke-HttpProbe -ProbeUrl $rootUrl -TimeoutSec $TimeoutSeconds

if ($rootResult.StatusCode -eq 200) {
    Write-Host "  [OK] Frontend Root SPA       : HTTP 200 OK" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Frontend Root SPA Failed: HTTP Code $($rootResult.StatusCode)" -ForegroundColor Red
    $overallPass = $false
}

# Probe version.json
Write-Host "  Probing Manifest: $versionUrl"
$versionResult = Invoke-HttpProbe -ProbeUrl $versionUrl -TimeoutSec $TimeoutSeconds

if ($versionResult.StatusCode -eq 200 -and $versionResult.Body) {
    try {
        $verJson = $versionResult.Body | ConvertFrom-Json
        $appVer = if ($verJson.version) { $verJson.version } else { $verJson.appVersion }
        $sha = $verJson.commitSha
        $displayVer = if ($appVer) { $appVer } else { "N/A" }
        Write-Host "  [OK] Artifact Version        : $displayVer" -ForegroundColor Green
        if ($sha) {
            Write-Host "  [OK] Build Commit SHA        : $sha" -ForegroundColor Green
        }
    } catch {
        Write-Host "  [FAIL] /version.json response returned HTTP 200 but was not valid JSON." -ForegroundColor Red
        $overallPass = $false
    }
} else {
    Write-Host "  [FAIL] Version Manifest Failed : HTTP Code $($versionResult.StatusCode) (/version.json)" -ForegroundColor Red
    $overallPass = $false
}
Write-Host ""

# ==============================================================================
# FINAL HEALTH SUMMARY
# ==============================================================================
Write-Host "======================================================================" -ForegroundColor Cyan
if ($overallPass) {
    Write-Host "   [PASS] ALL CHECKED HA HEALTH TIERS ARE HEALTHY & OPERATIONAL   " -ForegroundColor Green
    Write-Host "======================================================================" -ForegroundColor Cyan
    exit 0
} else {
    Write-Host "   [FAIL] ONE OR MORE HA HEALTH TIERS REPORTED DEGRADED STATE    " -ForegroundColor Red
    Write-Host "======================================================================" -ForegroundColor Cyan
    exit 1
}
