#Requires -Version 5.1
<#
.SYNOPSIS
    Production deployment and self-healing auto-rollback orchestrator for Gym Management.
.DESCRIPTION
    Automates pre-migration non-locking SQL backups, 10-snapshot retention rotation,
    Actuator health polling (5s interval, 90s timeout), crash-loop detection,
    and automatic container rollback to LAST_STABLE_VERSION upon release failure.
.PARAMETER AppVersion
    Target Artifact Version to deploy (defaults to $env:APP_VERSION or Git SemVer).
.PARAMETER RestoreBackup
    Path to a .sql.gz backup file for one-command emergency database recovery.
#>
[CmdletBinding()]
param(
    [string] $AppVersion = '',
    [string] $RestoreBackup = '',
    [switch] $Help
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if ($Help) {
    Write-Host 'Usage: .\scripts\deploy-prod.ps1 [-AppVersion <version>] [-RestoreBackup <path.sql.gz>]'
    exit 0
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = (Resolve-Path (Join-Path $scriptDir '..')).Path
Set-Location $repoRoot

$composeFile = 'docker-compose.prod.yml'
$stateFile = '.deploy-state'
$historyLog = '.deploy-history.log'
$mysqlContainer = 'gym-prod-mysql'
$backendContainer = 'gym-prod-backend'
$frontendContainer = 'gym-prod-frontend'

$dbName = if ($env:DB_NAME) { $env:DB_NAME } else { 'gym_management' }
$dbPassword = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { '123456' }
$backupDir = if ($env:BACKUP_DIR) { $env:BACKUP_DIR } else { './backups' }
$backupRetentionCount = if ($env:BACKUP_RETENTION_COUNT) { [int]$env:BACKUP_RETENTION_COUNT } else { 10 }
$healthcheckTimeout = if ($env:HEALTHCHECK_TIMEOUT_SECONDS) { [int]$env:HEALTHCHECK_TIMEOUT_SECONDS } else { 90 }
$healthcheckInterval = 5
$dockerRegistry = if ($env:DOCKER_REGISTRY) { $env:DOCKER_REGISTRY } else { 'local' }
$env:DOCKER_REGISTRY = $dockerRegistry

function Wait-MySQLReady {
    param([int]$MaxWaitSeconds = 30)
    for ($i = 0; $i -lt $MaxWaitSeconds; $i++) {
        & docker exec $mysqlContainer sh -ec "export MYSQL_PWD='$dbPassword'; mysqladmin --protocol=socket --user=root ping --silent" 2>$null
        if ($LASTEXITCODE -eq 0) {
            return $true
        }
        Start-Sleep -Seconds 1
    }
    return $false
}

function Test-SqlDumpArchive {
    param([string]$FilePath)
    if (-not (Test-Path $FilePath -PathType Leaf)) {
        Write-Error "Backup file does not exist: $FilePath"
        return $false
    }
    $item = Get-Item $FilePath
    if ($item.Length -eq 0) {
        Write-Error "Backup file is empty (0 bytes): $FilePath"
        return $false
    }
    try {
        $fs = [System.IO.File]::OpenRead($item.FullName)
        $gz = [System.IO.Compression.GZipStream]::new($fs, [System.IO.Compression.CompressionMode]::Decompress)
        $sr = [System.IO.StreamReader]::new($gz, [System.Text.Encoding]::UTF8)
        
        # Read first 4KB to verify SQL dump header
        $headerBuffer = New-Object char[] 4096
        $readCount = $sr.Read($headerBuffer, 0, $headerBuffer.Length)
        $headerText = [string]::new($headerBuffer, 0, $readCount)
        
        # Drain the rest of the stream to ensure gzip integrity
        $drainBuffer = New-Object char[] 8192
        while ($sr.Read($drainBuffer, 0, $drainBuffer.Length) -gt 0) {}
        
        $sr.Close()
        $gz.Close()
        $fs.Close()

        if ($headerText -notmatch '(MySQL dump|Server version|CREATE TABLE|SET @)') {
            Write-Error "Backup archive does not contain a valid SQL dump header: $FilePath"
            return $false
        }
        return $true
    }
    catch {
        Write-Error "Archive integrity check failed: $_"
        return $false
    }
}

function Test-ActuatorHealth {
    param([string]$Container)
    # Check for HTTP 200 and top-level "status":"UP" (prevent false positives on nested component states)
    $output = & docker exec $Container wget --server-response -qO- http://127.0.0.1:8080/actuator/health 2>&1
    $outputStr = ($output -join "`n")
    if ($outputStr -match 'HTTP/[0-9.]*\s+200' -and $outputStr -match '"status"\s*:\s*"UP"') {
        return $true
    }
    return $false
}

function Test-FrontendHealth {
    param([string]$Container)
    & docker exec $Container wget -q --spider http://127.0.0.1:80 2>$null
    return ($LASTEXITCODE -eq 0)
}

function Wait-ApplicationHealth {
    param(
        [int]$MaxAttempts,
        [bool]$CheckCrash = $true
    )
    for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++) {
        Write-Host "Healthcheck probe attempt $attempt/$MaxAttempts..." -ForegroundColor Cyan

        if ($CheckCrash) {
            $containerInspect = & docker inspect --format '{{.State.Status}}|{{.State.Restarting}}|{{.State.ExitCode}}' $backendContainer 2>$null
            if ($LASTEXITCODE -eq 0 -and $containerInspect) {
                $parts = $containerInspect.ToString().Trim().Split('|')
                $status = $parts[0]
                $restarting = $parts[1]
                $exitCode = $parts[2]

                if ($status -eq 'exited' -or $status -eq 'dead' -or $restarting -eq 'true' -or ($exitCode -ne '0' -and $status -ne 'running')) {
                    Write-Host "[CRASH DETECTED] Backend container '$backendContainer' failed or exited (Status: $status, ExitCode: $exitCode)." -ForegroundColor Red
                    return $false
                }
            }
        }

        if ((Test-ActuatorHealth -Container $backendContainer) -and (Test-FrontendHealth -Container $frontendContainer)) {
            Write-Host "[HEALTHY] Both Backend /actuator/health and Frontend HTTP 200 verified healthy on attempt $attempt." -ForegroundColor Green
            return $true
        }

        Start-Sleep -Seconds $healthcheckInterval
    }
    return $false
}

# ------------------------------------------------------------------------------
# 1. One-command Disaster Recovery: -RestoreBackup
# ------------------------------------------------------------------------------
if ($RestoreBackup) {
    Write-Host '================================================================================' -ForegroundColor Yellow
    Write-Host " [EMERGENCY RESTORE] Initiating database restore from: $RestoreBackup" -ForegroundColor Yellow
    Write-Host '================================================================================' -ForegroundColor Yellow

    if (-not (Test-SqlDumpArchive -FilePath $RestoreBackup)) {
        exit 1
    }

    Write-Host "Ensuring MySQL container is running..." -ForegroundColor Cyan
    & docker compose -f $composeFile up -d mysql

    Write-Host "Verifying MySQL container readiness..." -ForegroundColor Cyan
    if (-not (Wait-MySQLReady)) {
        Write-Error 'MySQL container did not become ready for restore.'
        exit 1
    }

    Write-Host "Decompressing and restoring SQL dump into MySQL database '$dbName'..." -ForegroundColor Cyan
    $backupItem = Get-Item $RestoreBackup
    $fs = [System.IO.File]::OpenRead($backupItem.FullName)
    $gz = [System.IO.Compression.GZipStream]::new($fs, [System.IO.Compression.CompressionMode]::Decompress)
    $sr = [System.IO.StreamReader]::new($gz, [System.Text.Encoding]::UTF8)
    $sqlContent = $sr.ReadToEnd()
    $sr.Close()
    $gz.Close()
    $fs.Close()

    $sqlContent | & docker exec -i $mysqlContainer sh -ec "export MYSQL_PWD='$dbPassword'; mysql --protocol=socket --user=root '$dbName'"
    if ($LASTEXITCODE -eq 0) {
        Write-Host "[SUCCESS] Database restored successfully from $RestoreBackup." -ForegroundColor Green
        exit 0
    }
    else {
        Write-Error 'Database restoration failed.'
        exit 1
    }
}

# ------------------------------------------------------------------------------
# 2. Version & State Resolution
# ------------------------------------------------------------------------------
if (-not $AppVersion) {
    if ($env:APP_VERSION) {
        $AppVersion = $env:APP_VERSION
    }
    elseif (Get-Command git -ErrorAction SilentlyContinue) {
        $gitSha = (& git rev-parse --short HEAD 2>$null)
        if ($LASTEXITCODE -eq 0 -and $gitSha) {
            $AppVersion = "1.0.0-$($gitSha.Trim())"
        }
        else {
            $AppVersion = '1.0.0-dev'
        }
    }
    else {
        $AppVersion = '1.0.0-dev'
    }
}

$env:APP_VERSION = $AppVersion

# Read LAST_STABLE_VERSION from .deploy-state
$lastStableVersion = ''
if (Test-Path $stateFile -PathType Leaf) {
    $match = Select-String -Path $stateFile -Pattern '^LAST_STABLE_VERSION=(.*)$' | Select-Object -First 1
    if ($match) {
        $lastStableVersion = $match.Matches.Groups[1].Value.Trim()
    }
}
if (-not $lastStableVersion) {
    $lastStableVersion = $AppVersion
}

Write-Host '================================================================================' -ForegroundColor Cyan
Write-Host ' [DEPLOY] Starting production deployment orchestrator' -ForegroundColor Cyan
Write-Host " Target Artifact Version : $AppVersion"
Write-Host " Last Stable Version     : $lastStableVersion"
Write-Host " Compose Stack           : $composeFile"
Write-Host " Backup Directory        : $backupDir"
Write-Host " Backup Retention Ring   : $backupRetentionCount snapshots"
Write-Host " Health Timeout          : ${healthcheckTimeout}s (every ${healthcheckInterval}s)"
Write-Host '================================================================================' -ForegroundColor Cyan

# ------------------------------------------------------------------------------
# 3. Ensure MySQL Readiness & Query Flyway Schema Version
# ------------------------------------------------------------------------------
Write-Host "Ensuring MySQL container is running..." -ForegroundColor Cyan
& docker compose -f $composeFile up -d mysql

Write-Host 'Verifying MySQL container readiness...' -ForegroundColor Cyan
if (-not (Wait-MySQLReady)) {
    Write-Error "Pre-deployment MySQL healthcheck failed. Aborting release immediately."
    exit 1
}

Write-Host 'Querying current schema version from flyway_schema_history...' -ForegroundColor Cyan
$schemaQuery = "SELECT COALESCE((SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1), '0');"
$rawSchema = & docker exec $mysqlContainer sh -ec "export MYSQL_PWD='$dbPassword'; mysql --protocol=socket --user=root --batch --skip-column-names -e `"$schemaQuery`" '$dbName'" 2>$null
$schemaVer = if ($rawSchema) { $rawSchema.ToString().Trim() } else { '0' }
if (-not $schemaVer) { $schemaVer = '0' }
Write-Host "Current Flyway schema version: v$schemaVer"

# ------------------------------------------------------------------------------
# 4. Pre-Migration Non-Locking Compressed Backup & Retention Pruning
# ------------------------------------------------------------------------------
if (-not (Test-Path $backupDir -PathType Container)) {
    $null = New-Item -ItemType Directory -Path $backupDir -Force
}

$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$backupFilename = "gym_db_v${schemaVer}_to_${AppVersion}_${timestamp}.sql.gz"
$backupFilePath = Join-Path $backupDir $backupFilename

Write-Host "Capturing non-locking pre-migration backup ($backupFilePath)..." -ForegroundColor Cyan
$dumpCmd = "export MYSQL_PWD='$dbPassword'; mysqldump --protocol=socket --user=root --single-transaction --routines --triggers --events '$dbName'"

# Execute mysqldump (redirect stderr to null so warnings do not contaminate SQL output)
$dumpOutput = & docker exec $mysqlContainer sh -ec $dumpCmd 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Error 'Pre-migration mysqldump failed! Aborting release immediately.'
    exit 1
}

$dumpBytes = [System.Text.Encoding]::UTF8.GetBytes(($dumpOutput -join "`n"))
if ($dumpBytes.Length -eq 0) {
    Write-Error 'Pre-migration backup is empty (0 bytes)! Aborting release immediately.'
    exit 1
}

# Compress into .sql.gz
$fsOut = [System.IO.File]::Create($backupFilePath)
$gzOut = [System.IO.Compression.GZipStream]::new($fsOut, [System.IO.Compression.CompressionLevel]::Optimal)
$gzOut.Write($dumpBytes, 0, $dumpBytes.Length)
$gzOut.Close()
$fsOut.Close()

# Verify backup existence, integrity, and SQL header
if (-not (Test-SqlDumpArchive -FilePath $backupFilePath)) {
    if (Test-Path $backupFilePath) { Remove-Item -Path $backupFilePath -Force }
    Write-Error 'Pre-migration backup archive failed validation! Aborting release immediately.'
    exit 1
}

$createdBackup = Get-Item $backupFilePath
Write-Host "[BACKUP VERIFIED] Snapshot created successfully: $backupFilePath ($($createdBackup.Length) bytes)" -ForegroundColor Green

# Enforce 10-snapshot retention ring (sorted by modification time, oldest first)
Write-Host "Enforcing backup retention ring (max $backupRetentionCount snapshots)..." -ForegroundColor Cyan
$existingBackups = Get-ChildItem -Path $backupDir -Filter '*.sql.gz' | Sort-Object LastWriteTime, CreationTime
if ($existingBackups.Count -gt $backupRetentionCount) {
    $toRemoveCount = $existingBackups.Count - $backupRetentionCount
    $toRemove = $existingBackups | Select-Object -First $toRemoveCount
    foreach ($old in $toRemove) {
        Write-Host "Pruning older backup: $($old.Name)" -ForegroundColor Yellow
        Remove-Item -Path $old.FullName -Force
    }
}

# ------------------------------------------------------------------------------
# 5. Launch Target Release via Compose Stack
# ------------------------------------------------------------------------------
Write-Host "Launching target Artifact Version $AppVersion via Compose..." -ForegroundColor Cyan
& docker compose -f $composeFile up -d
$composeExitCode = $LASTEXITCODE

$maxAttempts = [Math]::Floor($healthcheckTimeout / $healthcheckInterval)
$releaseHealthy = $false

if ($composeExitCode -ne 0) {
    Write-Host "[ERROR] 'docker compose up -d' failed for Artifact Version $AppVersion." -ForegroundColor Red
}
else {
    # ------------------------------------------------------------------------------
    # 6. Deep Actuator Health & Frontend Readiness Polling Loop
    # ------------------------------------------------------------------------------
    Write-Host "Polling health probes for up to ${healthcheckTimeout}s..." -ForegroundColor Cyan
    $releaseHealthy = Wait-ApplicationHealth -MaxAttempts $maxAttempts -CheckCrash $true
}

# ------------------------------------------------------------------------------
# 7. Release Evaluation: Success vs Auto-Rollback
# ------------------------------------------------------------------------------
$logTimestamp = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'

if ($releaseHealthy) {
    Write-Host '================================================================================' -ForegroundColor Green
    Write-Host " [RELEASE SUCCESS] Artifact Version $AppVersion is UP and healthy!" -ForegroundColor Green
    Write-Host '================================================================================' -ForegroundColor Green

    # Persist LAST_STABLE_VERSION in .deploy-state
    "LAST_STABLE_VERSION=$AppVersion" | Set-Content -Path $stateFile -Encoding utf8
    Write-Host "Updated $stateFile with LAST_STABLE_VERSION=$AppVersion"

    # Record in .deploy-history.log
    "[$logTimestamp] [SUCCESS] Version $AppVersion deployed successfully. Pre-migration backup: $backupFilename" | Add-Content -Path $historyLog -Encoding utf8
    exit 0
}
else {
    Write-Host '================================================================================' -ForegroundColor Red
    Write-Host ' [RELEASE FAILED] Health verification timed out or container crashed!' -ForegroundColor Red
    Write-Host ' Printing last 50 lines of backend logs for diagnosis:' -ForegroundColor Red
    Write-Host '================================================================================' -ForegroundColor Red
    & docker logs --tail 50 $backendContainer 2>&1

    Write-Host '================================================================================' -ForegroundColor Yellow
    Write-Host " [AUTO-ROLLBACK] Triggering automated rollback to LAST_STABLE_VERSION=$lastStableVersion" -ForegroundColor Yellow
    Write-Host '================================================================================' -ForegroundColor Yellow

    $env:APP_VERSION = $lastStableVersion
    & docker compose -f $composeFile up -d

    Write-Host 'Verifying rolled-back stack health (Backend and Frontend)...' -ForegroundColor Cyan
    $rollbackHealthy = Wait-ApplicationHealth -MaxAttempts $maxAttempts -CheckCrash $false

    # Record rollback event in .deploy-history.log
    "[$logTimestamp] [ROLLED_BACK] Version $AppVersion failed health verification. Auto-rolled back to LAST_STABLE_VERSION=$lastStableVersion (Rollback Healthy: $rollbackHealthy)." | Add-Content -Path $historyLog -Encoding utf8

    Write-Error "Release $AppVersion failed. Rollback executed. Exit code 1."
    exit 1
}
