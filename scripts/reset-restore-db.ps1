#Requires -Version 5.1
[CmdletBinding()]
param(
    [ValidatePattern('^[A-Za-z0-9][A-Za-z0-9_.-]*$')]
    [string] $ContainerName = 'gym-management-mysql',

    # 0 discovers the host port mapped to MySQL's 3306 port.
    [ValidateRange(0, 65535)]
    [int] $HostPort = 0,

    [ValidateRange(10, 120)]
    [int] $TimeoutSeconds = 30,

    # Skips the typed RESET prompt. Use only for an isolated test database.
    [switch] $Force
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-ContainerSql {
    param(
        [Parameter(Mandatory)][string] $Sql,
        [switch] $UseGymManagementDatabase
    )

    # Keep the MySQL password inside the container; it is never added to the
    # docker command line or printed in the script output.
    $databaseOption = if ($UseGymManagementDatabase) { '--database=gym_management' } else { '' }
    $shellCommand = 'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; mysql --protocol=socket --user=root --batch --raw --skip-column-names ' + $databaseOption + ' --execute="' + $Sql + '"'
    $output = & docker exec $ContainerName sh -ec $shellCommand 2>&1
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
    throw "MySQL container '$ContainerName' was not found. Start the compatible local MySQL container first; do not attach a MySQL 8.0 image to a volume initialized by MySQL 8.4."
}
$containerDetails = $inspect.ToString().Trim().Split('|')
$containerState = $containerDetails[0]
$containerImage = $containerDetails[1]
if ($containerImage -notmatch '(^|/)mysql:8\.') {
    throw "Container '$ContainerName' uses '$containerImage'. This reset script only supports a MySQL 8.x container."
}

if ($HostPort -eq 0) {
    $portOutput = & docker port $ContainerName '3306/tcp' 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Could not discover a published host port for '$ContainerName' port 3306."
    }
    $portMatch = [regex]::Match(($portOutput -join "`n"), ':(\d+)(?:\r?\n|$)')
    if (-not $portMatch.Success) {
        throw "MySQL container '$ContainerName' does not publish port 3306 to the host."
    }
    $HostPort = [int] $portMatch.Groups[1].Value
}

if (-not $Force) {
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
    Start-Sleep -Milliseconds 500
}
if (-not $ready) {
    throw "MySQL in '$ContainerName' did not become ready within $TimeoutSeconds seconds. Database was not reset."
}

Write-Host 'Dropping and recreating gym_management...'
Invoke-ContainerSql -Sql 'DROP DATABASE IF EXISTS gym_management; CREATE DATABASE gym_management CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;' | Out-Null

# Use the same development profile the Tester uses for demo data migrations.
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
    $dbPassword = '123456' # Matches backend/docker-compose.yml default.
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
    $mavenPath = if ($mavenCommand) { $mavenCommand.Source } else { $null }

    # Prefer an already-cached Maven distribution when PATH has no Maven.
    # This also avoids wrapper startup issues on some Windows PowerShell hosts.
    if (-not $mavenPath) {
        $wrapperProperties = Join-Path $backendPath '.mvn/wrapper/maven-wrapper.properties'
        $propertiesText = Get-Content $wrapperProperties -Raw
        if ($propertiesText -match 'apache-maven-(\d+\.\d+\.\d+)-bin\.zip') {
            $mavenVersion = $Matches[1]
            $cacheRoot = Join-Path $env:USERPROFILE '.m2/wrapper/dists'
            if (Test-Path $cacheRoot -PathType Container) {
                $cacheDirectories = Get-ChildItem $cacheRoot -Directory | Where-Object Name -like "apache-maven-$mavenVersion*"
                foreach ($directory in $cacheDirectories) {
                    $cachedCommand = Get-ChildItem $directory.FullName -Filter 'mvn.cmd' -File -Recurse | Select-Object -First 1
                    if ($cachedCommand) {
                        $mavenPath = $cachedCommand.FullName
                        break
                    }
                }
            }
        }
    }
    if (-not $mavenPath) {
        $mavenPath = Join-Path $backendPath 'mvnw.cmd'
    }
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

    Write-Host 'Running Spring Boot in non-web mode so Flyway can apply all available migrations...'
    $logKey = '{0:yyyyMMddHHmmss}-{1}' -f (Get-Date), $PID
    $mavenStdout = Join-Path $env:TEMP "gym-management-flyway-$logKey.out.log"
    $mavenStderr = Join-Path $env:TEMP "gym-management-flyway-$logKey.err.log"
    $process = Start-Process -FilePath $mavenPath -ArgumentList $mavenArguments -WorkingDirectory $backendPath -PassThru -WindowStyle Hidden -RedirectStandardOutput $mavenStdout -RedirectStandardError $mavenStderr
    $remainingMilliseconds = [math]::Max(1, [int] (($TimeoutSeconds - $timer.Elapsed.TotalSeconds) * 1000))
    if (-not $process.WaitForExit($remainingMilliseconds)) {
        $null = & taskkill.exe /PID $process.Id /T /F 2>&1
        foreach ($logFile in @($mavenStdout, $mavenStderr)) {
            if (Test-Path $logFile -PathType Leaf) {
                Get-Content $logFile -Tail 25 | ForEach-Object { Write-Host $_ }
            }
        }
        throw "Backend/Flyway exceeded the $TimeoutSeconds-second limit. Review Maven output in $env:TEMP."
    }
    $process.Refresh()
    if ($process.ExitCode -ne 0) {
        foreach ($logFile in @($mavenStdout, $mavenStderr)) {
            if (Test-Path $logFile -PathType Leaf) {
                Get-Content $logFile -Tail 35 | ForEach-Object { Write-Host $_ }
            }
        }
        throw "Backend/Flyway exited with code $($process.ExitCode). Review Maven output in $env:TEMP."
    }

    Get-Content $mavenStdout | Where-Object { $_ -match 'Flyway|Successfully applied .* migration|Started GymManagementApiApplication' } | ForEach-Object { Write-Host $_ }
}
finally {
    foreach ($name in $environmentNames) {
        [Environment]::SetEnvironmentVariable($name, $previousEnvironment[$name], 'Process')
    }
}

$history = Invoke-ContainerSql -Sql "SELECT COALESCE(GROUP_CONCAT(CONCAT('V', version) ORDER BY installed_rank SEPARATOR ', '), 'none') FROM flyway_schema_history WHERE success = 1;" -UseGymManagementDatabase
$validation = Invoke-ContainerSql -Sql "SELECT CONCAT(COUNT(*), '|', SUM(stock BETWEEN 50 AND 100), '|', SUM(stock <= min_stock AND status = 'ACTIVE'), '|', SUM(stock IN (1, 2)), '|', SUM(stock = 0 AND status = 'ACTIVE'), '|', SUM(status = 'INACTIVE'), '|', (SELECT COUNT(*) FROM categories WHERE code IN ('CAT-SUPPLEMENT', 'CAT-BEVERAGE', 'CAT-ACCESSORY')), '|', (SELECT COUNT(*) FROM products p LEFT JOIN categories c ON c.id = p.category_id WHERE c.id IS NULL)) FROM products WHERE sku IN ('SUP-WHEY-VAN-001', 'SUP-CREATINE-300-001', 'DRK-ELECTRO-ORANGE-001', 'ACC-GLOVE-GYM-001', 'ACC-SHAKER-700-001', 'SUP-WHEY-CHOCO-001', 'SUP-CREATINE-100-001', 'DRK-ELECTRO-LEMON-001', 'ACC-RESISTANCE-BAND-001', 'ACC-SPEED-ROPE-001');" -UseGymManagementDatabase
$expectedValidation = '10|4|5|2|1|1|3|0'
if ($validation -ne $expectedValidation) {
    throw "Database was migrated but seed validation failed. Expected '$expectedValidation'; got '$validation'."
}

$timer.Stop()
if ($timer.Elapsed.TotalSeconds -gt $TimeoutSeconds) {
    throw "Database was restored successfully in $([math]::Round($timer.Elapsed.TotalSeconds, 1)) seconds, exceeding the $TimeoutSeconds-second target."
}

Write-Host "Database gym_management has been restored. Flyway history: $history"
Write-Host 'Verified 10 seed products: 4 plentiful, 5 active low-stock, 2 with 1-2 units, 1 active out-of-stock, 1 inactive; 3 categories; 0 orphaned products.'
Write-Host ("Completed in {0:N1} seconds." -f $timer.Elapsed.TotalSeconds)
