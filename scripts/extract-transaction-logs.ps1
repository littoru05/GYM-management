#Requires -Version 5.1
[CmdletBinding()]
param(
    [string] $ContainerName = 'gym-management-backend',
    [string] $LocalLogPath = '',
    [int] $Lines = 1000,
    [string] $OutputPath = '',
    [switch] $All,
    [switch] $LocalOnly,
    [switch] $DockerOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = (Resolve-Path (Join-Path $scriptDir '..')).Path
$reportsDir = Join-Path $repoRoot 'docs/testing/reports/logs'

if (-not (Test-Path $reportsDir -PathType Container)) {
    $null = New-Item -ItemType Directory -Path $reportsDir -Force
}

$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
if (-not $OutputPath) {
    $suffix = if ($All) { 'full' } else { 'summary' }
    $OutputPath = Join-Path $reportsDir "txn-rollback-${timestamp}-${suffix}.log"
}

# Determine source: Docker vs Local file
$sourceType = 'NONE'
$rawLines = @()

$dockerAvailable = [bool](Get-Command docker -ErrorAction SilentlyContinue)

if (-not $LocalOnly -and $dockerAvailable) {
    $inspect = & docker inspect --format '{{.State.Status}}' $ContainerName 2>$null
    if ($LASTEXITCODE -eq 0 -and $inspect -and $inspect.ToString().Trim() -eq 'running') {
        $sourceType = "Docker ($ContainerName)"
        Write-Host "Fetching logs from Docker container '$ContainerName' (tail $Lines)..." -ForegroundColor Cyan
        $rawOutput = & docker logs --tail $Lines $ContainerName 2>&1
        $rawLines = @($rawOutput | ForEach-Object { $_.ToString() })
    }
}

if ($sourceType -eq 'NONE' -and (-not $DockerOnly)) {
    if (-not $LocalLogPath) {
        $defaultLocalLog = Join-Path $repoRoot 'backend/logs/gym-backend.log'
        if (Test-Path $defaultLocalLog -PathType Leaf) {
            $LocalLogPath = $defaultLocalLog
        }
        else {
            $cwdLog = Join-Path (Get-Location) 'logs/gym-backend.log'
            if (Test-Path $cwdLog -PathType Leaf) {
                $LocalLogPath = $cwdLog
            }
        }
    }

    if ($LocalLogPath -and (Test-Path $LocalLogPath -PathType Leaf)) {
        $sourceType = "Local File ($LocalLogPath)"
        Write-Host "Reading logs from local file '$LocalLogPath' (tail $Lines)..." -ForegroundColor Cyan
        $rawLines = @(Get-Content -Path $LocalLogPath -Tail $Lines -Encoding UTF8)
    }
}

if ($sourceType -eq 'NONE') {
    Write-Host "[WARNING] Could not find running backend container '$ContainerName' or local log file 'backend/logs/gym-backend.log'." -ForegroundColor Yellow
    Write-Host "Tips:" -ForegroundColor Yellow
    Write-Host " 1. If running with Docker, ensure container is started: docker compose up -d backend" -ForegroundColor Yellow
    Write-Host " 2. If running locally with Spring Boot, make sure application-dev.yaml has logging.file.name enabled." -ForegroundColor Yellow
    Write-Host " 3. You can pass a custom log path with: -LocalLogPath <path_to_log_file>" -ForegroundColor Yellow
    exit 1
}

$filterPatterns = @(
    'JpaTransactionManager',
    'Initiating transaction rollback',
    'Rolling back JPA transaction',
    'Creating new transaction',
    'Completed transaction',
    'POS Checkout',
    'InsufficientStockException',
    'PosOrderServiceImpl',
    'MemberSubscriptionServiceImpl',
    'VoucherServiceImpl',
    'voucher_usages',
    'voucher',
    'Voucher',
    'ERROR',
    'Exception',
    'rollback'
)
$combinedRegex = [regex]::new(($filterPatterns -join '|'), [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)

$filteredLines = [System.Collections.Generic.List[string]]::new()
$rollbackCount = 0
$insufficientStockCount = 0
$posOrderCount = 0
$voucherEventCount = 0
$exceptionCount = 0

foreach ($line in $rawLines) {
    if ($All) {
        $filteredLines.Add($line)
    }
    elseif ($combinedRegex.IsMatch($line)) {
        $filteredLines.Add($line)
    }

    if ($line -match 'rollback|Rolling back JPA transaction') {
        $rollbackCount++
    }
    if ($line -match 'InsufficientStockException') {
        $insufficientStockCount++
    }
    if ($line -match 'POS Checkout|OrderResponseDTO') {
        $posOrderCount++
    }
    if ($line -match 'voucher|Voucher|voucher_usages') {
        $voucherEventCount++
    }
    if ($line -match 'ERROR|Exception') {
        $exceptionCount++
    }
}

# Add Header to log file
$reportContent = [System.Collections.Generic.List[string]]::new()
$reportContent.Add("================================================================================")
$reportContent.Add(" GYM MANAGEMENT - TRANSACTION & ROLLBACK AUDIT LOG EXTRACT")
$reportContent.Add("================================================================================")
$reportContent.Add(" Extracted At   : $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')")
$reportContent.Add(" Log Source     : $sourceType")
$reportContent.Add(" Mode           : $(if ($All) { 'FULL RAW DUMP' } else { 'FILTERED TRANSACTION AUDIT' })")
$reportContent.Add(" Lines Scanned  : $($rawLines.Count)")
$reportContent.Add(" Lines Matched  : $($filteredLines.Count)")
$reportContent.Add("--------------------------------------------------------------------------------")
$reportContent.Add(" SUMMARY METRICS FOR TESTER AUDIT:")
$reportContent.Add("  - Transaction Rollbacks Detected : $rollbackCount")
$reportContent.Add("  - Out-of-Stock (Stock Rollbacks) : $insufficientStockCount")
$reportContent.Add("  - POS Checkout Events            : $posOrderCount")
$reportContent.Add("  - Voucher Related Events         : $voucherEventCount")
$reportContent.Add("  - Exceptions / Errors Recorded   : $exceptionCount")
$reportContent.Add("================================================================================")
$reportContent.Add("")
$reportContent.AddRange($filteredLines)

[System.IO.File]::WriteAllLines($OutputPath, $reportContent, [System.Text.Encoding]::UTF8)

# Output summary to console
Write-Host "================================================================================" -ForegroundColor Green
Write-Host " [SUCCESS] Transaction logs extracted successfully!" -ForegroundColor Green
Write-Host "================================================================================" -ForegroundColor Green
Write-Host " Log Source     : $sourceType"
Write-Host " Lines Scanned  : $($rawLines.Count)"
Write-Host " Lines Extracted: $($filteredLines.Count)"
Write-Host "--------------------------------------------------------------------------------"
Write-Host " AUDIT SUMMARY METRICS:" -ForegroundColor Yellow
Write-Host "  * Transaction Rollbacks : $rollbackCount" -ForegroundColor $(if ($rollbackCount -gt 0) { 'Green' } else { 'Gray' })
Write-Host "  * Stock Exceptions      : $insufficientStockCount" -ForegroundColor $(if ($insufficientStockCount -gt 0) { 'Green' } else { 'Gray' })
Write-Host "  * POS Checkout Events   : $posOrderCount"
Write-Host "  * Voucher Events        : $voucherEventCount"
Write-Host "  * Errors / Exceptions   : $exceptionCount"
Write-Host "--------------------------------------------------------------------------------"
Write-Host " [SAVED] Log file evidence: $OutputPath" -ForegroundColor Cyan
Write-Host " Tester (Thanh) can attach this log file directly to testing evidence documents."
Write-Host "================================================================================"
