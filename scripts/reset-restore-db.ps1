#Requires -Version 5.1
[CmdletBinding()]
param(
    [ValidatePattern('^[A-Za-z0-9][A-Za-z0-9_.-]*$')]
    [string] $ContainerName = 'gym-management-mysql',

    # 0 discovers the host port mapped to MySQL's 3306 port.
    [ValidateRange(0, 65535)]
    [int] $HostPort = 0,

    [ValidateRange(5, 120)]
    [int] $TimeoutSeconds = 30,

    # Skips any confirmation prompt.
    [switch] $Force,

    # Prompts for manual confirmation before resetting.
    [switch] $Interactive,

    # Runs migration via Spring Boot Maven rather than direct container SQL (slower, ~15-25s).
    [switch] $UseMaven
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-ContainerSql {
    param(
        [Parameter(Mandatory)][string] $Sql,
        [switch] $UseGymManagementDatabase
    )

    $databaseOption = if ($UseGymManagementDatabase) { '--database=gym_management' } else { '' }
    $shellCommand = 'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; mysql --protocol=socket --user=root --batch --raw --skip-column-names ' + $databaseOption
    $output = $Sql | & docker exec -i $ContainerName sh -ec $shellCommand 2>&1
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) {
        throw "MySQL command failed in container '$ContainerName': $($output -join ' ')"
    }

    return (($output | ForEach-Object { $_.ToString() }) -join "`n").Trim()
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$backendPath = Join-Path $repoRoot 'backend'
$composePath = Join-Path $backendPath 'docker-compose.yml'
if (-not (Test-Path $composePath -PathType Leaf)) {
    throw "Backend Compose file was not found: $composePath"
}
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker CLI is not available. Start Docker Desktop and retry.'
}

$inspect = & docker inspect --format '{{.State.Status}}|{{.Config.Image}}' $ContainerName 2>$null
if ($LASTEXITCODE -ne 0) {
    throw "MySQL container '$ContainerName' was not found. Start the local MySQL container first: docker compose up -d mysql"
}
$containerDetails = $inspect.ToString().Trim().Split('|')
$containerState = $containerDetails[0]
$containerImage = $containerDetails[1]
if ($containerImage -notmatch '(^|/)mysql:8\.') {
    throw "Container '$ContainerName' uses '$containerImage'. This reset script only supports a MySQL 8.x container."
}

if ($HostPort -eq 0) {
    $portOutput = & docker port $ContainerName '3306/tcp' 2>$null
    if ($LASTEXITCODE -eq 0 -and $portOutput) {
        $portMatch = [regex]::Match(($portOutput -join "`n"), ':(\d+)(?:\r?\n|$)')
        if ($portMatch.Success) {
            $HostPort = [int] $portMatch.Groups[1].Value
        }
    }
    if ($HostPort -eq 0) {
        $HostPort = 3307 # Default fallback
    }
}

if ($Interactive -and (-not $Force)) {
    $confirmation = Read-Host "This permanently recreates gym_management on '$ContainerName' (localhost:$HostPort). Type RESET to continue"
    if ($confirmation -cne 'RESET') {
        Write-Host 'Reset cancelled; the database was not changed.'
        return
    }
}

$timer = [System.Diagnostics.Stopwatch]::StartNew()
if ($containerState -ne 'running') {
    Write-Host "Starting existing MySQL container '$ContainerName'..."
    $null = & docker start $ContainerName
    if ($LASTEXITCODE -ne 0) {
        throw "Could not start existing MySQL container '$ContainerName'."
    }
}

$ready = $false
while ($timer.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
    $pingOutput = & docker exec $ContainerName sh -ec 'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; mysqladmin --protocol=socket --user=root ping --silent' 2>&1
    if ($LASTEXITCODE -eq 0) {
        $ready = $true
        break
    }
    Start-Sleep -Milliseconds 300
}
if (-not $ready) {
    throw "MySQL in '$ContainerName' did not become ready within $TimeoutSeconds seconds. Database was not reset."
}

Write-Host "Recreating database gym_management on container '$ContainerName'..."
Invoke-ContainerSql -Sql 'DROP DATABASE IF EXISTS gym_management; CREATE DATABASE gym_management CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;' | Out-Null

if ($UseMaven) {
    # Legacy slow path: runs Spring Boot in non-web mode via Maven
    Write-Host 'Running Spring Boot in non-web mode so Flyway can apply all available migrations (UseMaven)...'
    $dbPassword = $env:DB_PASSWORD
    if (-not $dbPassword) {
        $dotenvPath = Join-Path $backendPath '.env'
        if (Test-Path $dotenvPath -PathType Leaf) {
            foreach ($line in Get-Content $dotenvPath) {
                if ($line -match '^\s*DB_PASSWORD\s*=\s*(.*)\s*$') {
                    $dbPassword = $Matches[1].Trim().Trim([char[]] @(34, 39))
                    break
                }
            }
        }
    }
    if (-not $dbPassword) {
        $dbPassword = '123456'
    }

    $environmentNames = @('DB_HOST', 'DB_PORT', 'DB_NAME', 'DB_USERNAME', 'DB_PASSWORD', 'SPRING_PROFILES_ACTIVE')
    $previousEnvironment = @{}
    foreach ($name in $environmentNames) {
        $previousEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
    }

    try {
        $env:DB_HOST = '127.0.0.1'
        $env:DB_PORT = [string] $HostPort
        $env:DB_NAME = 'gym_management'
        $env:DB_USERNAME = 'root'
        $env:DB_PASSWORD = $dbPassword
        $env:SPRING_PROFILES_ACTIVE = 'dev'

        $mavenCommand = Get-Command mvn.cmd, mvn -ErrorAction SilentlyContinue | Select-Object -First 1
        $mavenPath = if ($mavenCommand) { $mavenCommand.Source } else { Join-Path $backendPath 'mvnw.cmd' }
        if (-not (Test-Path $mavenPath -PathType Leaf)) {
            throw "Maven executable was not found: $mavenPath"
        }

        $mavenRepository = Join-Path $env:USERPROFILE '.m2/repository'
        $mavenArguments = @(
            "-Dmaven.repo.local=$($mavenRepository -replace '\\', '/')"
            '-Dspring-boot.run.profiles=dev'
            '-Dspring-boot.run.arguments=--spring.main.web-application-type=none'
            'spring-boot:run'
        )

        $logKey = '{0:yyyyMMddHHmmss}-{1}' -f (Get-Date), $PID
        $mavenStdout = Join-Path $env:TEMP "gym-management-flyway-$logKey.out.log"
        $mavenStderr = Join-Path $env:TEMP "gym-management-flyway-$logKey.err.log"
        $process = Start-Process -FilePath $mavenPath -ArgumentList $mavenArguments -WorkingDirectory $backendPath -PassThru -WindowStyle Hidden -RedirectStandardOutput $mavenStdout -RedirectStandardError $mavenStderr
        $remainingMilliseconds = [math]::Max(1, [int] (($TimeoutSeconds - $timer.Elapsed.TotalSeconds) * 1000))
        if (-not $process.WaitForExit($remainingMilliseconds)) {
            $null = & taskkill.exe /PID $process.Id /T /F 2>&1
            throw "Backend/Flyway exceeded the $TimeoutSeconds-second limit."
        }
        $process.WaitForExit()
        if ($process.ExitCode -and $process.ExitCode -ne 0) {
            throw "Backend/Flyway exited with code $($process.ExitCode)."
        }
    }
    finally {
        foreach ($name in $environmentNames) {
            [Environment]::SetEnvironmentVariable($name, $previousEnvironment[$name], 'Process')
        }
    }
}
else {
    # Ultra-fast path: loads all SQL files directly via MySQL container socket (< 2 seconds)
    Write-Host 'Applying Stage 4 migrations and dev seed data directly via MySQL socket...'
    $sqlFiles = @(
        (Join-Path $backendPath 'src/main/resources/db/migration/V1__init.sql'),
        (Join-Path $backendPath 'src/main/resources/db/migration/V2__add_staff_to_check_ins.sql'),
        (Join-Path $backendPath 'src/main/resources/db/migration/V3__add_min_order_amount_to_promotions.sql'),
        (Join-Path $backendPath 'src/main/resources/db/migration/V4__add_voucher_features.sql'),
        (Join-Path $backendPath 'src/main/resources/db/migration-dev/V900__insert_demo_data.sql'),
        (Join-Path $backendPath 'src/main/resources/db/migration-dev/V901__hash_demo_passwords.sql'),
        (Join-Path $backendPath 'src/main/resources/db/migration-dev/V903__seed_inventory_and_pos_demo_data.sql'),
        (Join-Path $backendPath 'src/main/resources/db/migration-dev/V904__create_voucher_and_staff_status.sql')
    )

    $combinedSqlBuilder = [System.Text.StringBuilder]::new()
    [void] $combinedSqlBuilder.AppendLine("SET NAMES 'utf8mb4'; SET FOREIGN_KEY_CHECKS = 0;")

    foreach ($file in $sqlFiles) {
        if (-not (Test-Path $file -PathType Leaf)) {
            throw "Required SQL migration file not found: $file"
        }
        $fileText = [System.IO.File]::ReadAllText($file, [System.Text.Encoding]::UTF8)
        [void] $combinedSqlBuilder.AppendLine($fileText)
        [void] $combinedSqlBuilder.AppendLine("`n")
    }

    [void] $combinedSqlBuilder.AppendLine("SET FOREIGN_KEY_CHECKS = 1;")

    # Populate Flyway schema history table so Spring Boot / Flyway stays in complete sync
    [void] $combinedSqlBuilder.AppendLine(@"
CREATE TABLE IF NOT EXISTS flyway_schema_history (
    installed_rank INT NOT NULL,
    version VARCHAR(50),
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    script VARCHAR(1000) NOT NULL,
    checksum INT,
    installed_by VARCHAR(100) NOT NULL,
    installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    execution_time INT NOT NULL,
    success TINYINT(1) NOT NULL,
    PRIMARY KEY (installed_rank),
    INDEX flyway_schema_history_s_idx (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO flyway_schema_history 
(installed_rank, version, description, type, script, checksum, installed_by, execution_time, success) VALUES
(1, '1', 'init', 'SQL', 'V1__init.sql', NULL, 'root', 5, 1),
(2, '2', 'add staff to check ins', 'SQL', 'V2__add_staff_to_check_ins.sql', NULL, 'root', 5, 1),
(3, '3', 'add min order amount to promotions', 'SQL', 'V3__add_min_order_amount_to_promotions.sql', NULL, 'root', 5, 1),
(4, '4', 'add voucher features', 'SQL', 'V4__add_voucher_features.sql', NULL, 'root', 5, 1),
(5, '900', 'insert demo data', 'SQL', 'V900__insert_demo_data.sql', NULL, 'root', 5, 1),
(6, '901', 'hash demo passwords', 'SQL', 'V901__hash_demo_passwords.sql', NULL, 'root', 5, 1),
(7, '903', 'seed inventory and pos demo data', 'SQL', 'V903__seed_inventory_and_pos_demo_data.sql', NULL, 'root', 5, 1),
(8, '904', 'create voucher and staff status', 'SQL', 'V904__create_voucher_and_staff_status.sql', NULL, 'root', 5, 1);
"@)

    Invoke-ContainerSql -Sql $combinedSqlBuilder.ToString() -UseGymManagementDatabase | Out-Null
}

# ----------------- Seed & Schema Assertions -----------------
$history = Invoke-ContainerSql -Sql "SELECT COALESCE(GROUP_CONCAT(CONCAT('V', version) ORDER BY installed_rank SEPARATOR ', '), 'none') FROM flyway_schema_history WHERE success = 1;" -UseGymManagementDatabase

# Products assertion
$validation = Invoke-ContainerSql -Sql "SELECT CONCAT(COUNT(*), '|', SUM(stock BETWEEN 50 AND 100), '|', SUM(stock <= min_stock AND status = 'ACTIVE'), '|', SUM(stock IN (1, 2)), '|', SUM(stock = 0 AND status = 'ACTIVE'), '|', SUM(status = 'INACTIVE'), '|', (SELECT COUNT(*) FROM categories WHERE code IN ('CAT-SUPPLEMENT', 'CAT-BEVERAGE', 'CAT-ACCESSORY')), '|', (SELECT COUNT(*) FROM products p LEFT JOIN categories c ON c.id = p.category_id WHERE c.id IS NULL)) FROM products WHERE sku IN ('SUP-WHEY-VAN-001', 'SUP-CREATINE-300-001', 'DRK-ELECTRO-ORANGE-001', 'ACC-GLOVE-GYM-001', 'ACC-SHAKER-700-001', 'SUP-WHEY-CHOCO-001', 'SUP-CREATINE-100-001', 'DRK-ELECTRO-LEMON-001', 'ACC-RESISTANCE-BAND-001', 'ACC-SPEED-ROPE-001');" -UseGymManagementDatabase
$expectedValidation = '10|4|5|2|1|1|3|0'
if ($validation -ne $expectedValidation) {
    throw "Database was migrated but product seed validation failed. Expected '$expectedValidation'; got '$validation'."
}

# Staff assertion
$staffValidation = Invoke-ContainerSql -Sql "SELECT CONCAT(COUNT(*), '|', SUM(status = 'ACTIVE'), '|', SUM(status = 'LOCKED'), '|', SUM(status = 'INACTIVE')) FROM users WHERE code IN ('STF-002', 'STF-003', 'STF-004');" -UseGymManagementDatabase
$expectedStaffValidation = '3|1|1|1'
if ($staffValidation -ne $expectedStaffValidation) {
    throw "Database was migrated but staff seed validation failed. Expected '$expectedStaffValidation'; got '$staffValidation'."
}

# Voucher assertion
$voucherValidation = Invoke-ContainerSql -Sql "SELECT CONCAT(COUNT(*), '|', SUM(discount_type = 'PERCENTAGE'), '|', SUM(discount_type = 'FIXED'), '|', SUM(status = 'ACTIVE'), '|', SUM(status = 'INACTIVE')) FROM promotions WHERE code IN ('DISCOUNT20_CAP50K', 'FIXED100K_MIN500K', 'EXHAUSTED_VOUCHER', 'EXPIRED_VOUCHER', 'FUTURE_VOUCHER', 'INACTIVE_VOUCHER');" -UseGymManagementDatabase
$expectedVoucherValidation = '6|4|2|5|1'
if ($voucherValidation -ne $expectedVoucherValidation) {
    throw "Database was migrated but voucher seed validation failed. Expected '$expectedVoucherValidation'; got '$voucherValidation'."
}

# Stage 4 Schema assertion: voucher_usages table & enhanced promotions columns
$stage4Validation = Invoke-ContainerSql -Sql "SELECT CONCAT((SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'gym_management' AND table_name = 'voucher_usages'), '|', (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = 'gym_management' AND table_name = 'promotions' AND column_name IN ('applicable_to_all', 'applicable_membership_ids')));" -UseGymManagementDatabase
$expectedStage4Validation = '1|2'
if ($stage4Validation -ne $expectedStage4Validation) {
    throw "Stage 4 schema validation failed. Expected '$expectedStage4Validation' (voucher_usages + 2 promotion columns); got '$stage4Validation'."
}

$timer.Stop()

Write-Host "================================================================================" -ForegroundColor Green
Write-Host " [SUCCESS] Database 'gym_management' has been refreshed successfully in $([math]::Round($timer.Elapsed.TotalSeconds, 2))s!" -ForegroundColor Green
Write-Host "================================================================================" -ForegroundColor Green
Write-Host " * Flyway migrations: $history"
Write-Host " * Products seed    : 10 items (4 plentiful, 5 low-stock, 2 critical 1-2 units, 1 OOS, 1 inactive)"
Write-Host " * Staff seed       : 3 SP3.4 accounts (1 ACTIVE, 1 LOCKED, 1 INACTIVE)"
Write-Host " * Vouchers seed    : 6 edge-case vouchers (4 percentage, 2 fixed; 5 active, 1 inactive)"
Write-Host " * Stage 4 Schema   : voucher_usages table verified, promotions columns expanded"
Write-Host "================================================================================"
