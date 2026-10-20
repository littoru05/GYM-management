# ==============================================================================
# SUB-2 AUTOMATED VERIFICATION SCRIPT
# Validates Network Configuration, .env Standards, CORS, and Chặng 4 API Flow
# ==============================================================================

$ErrorActionPreference = "Stop"
$failures = 0
$passes = 0

function Assert-Check {
    param(
        [string]$Name,
        [bool]$Condition,
        [string]$Details = ""
    )
    if ($Condition) {
        Write-Host " [PASS] $Name" -ForegroundColor Green
        $script:passes++
    } else {
        Write-Host " [FAIL] $Name - $Details" -ForegroundColor Red
        $script:failures++
    }
}

Write-Host "=== 1. Checking .env.example Standardization ===" -ForegroundColor Cyan

# 1.1 Root .env.example
$rootEnvPath = Join-Path $PSScriptRoot "..\.env.example"
Assert-Check -Name "Root .env.example exists" -Condition (Test-Path $rootEnvPath)
if (Test-Path $rootEnvPath) {
    $content = Get-Content $rootEnvPath -Raw
    Assert-Check -Name "Root .env defines FRONTEND_PORT" -Condition ($content -match "FRONTEND_PORT=")
    Assert-Check -Name "Root .env defines BACKEND_PORT" -Condition ($content -match "BACKEND_PORT=")
    Assert-Check -Name "Root .env defines FRONTEND_ORIGIN" -Condition ($content -match "FRONTEND_ORIGIN=")
    Assert-Check -Name "Root .env defines JWT_SECRET" -Condition ($content -match "JWT_SECRET=")
    Assert-Check -Name "Root .env defines VITE_USE_MOCK" -Condition ($content -match "VITE_USE_MOCK=")
}

# 1.2 Backend .env.example
$backendEnvPath = Join-Path $PSScriptRoot "..\backend\.env.example"
Assert-Check -Name "Backend .env.example exists" -Condition (Test-Path $backendEnvPath)
if (Test-Path $backendEnvPath) {
    $content = Get-Content $backendEnvPath -Raw
    Assert-Check -Name "Backend .env defines SERVER_PORT" -Condition ($content -match "SERVER_PORT=")
    Assert-Check -Name "Backend .env defines FRONTEND_ORIGIN" -Condition ($content -match "FRONTEND_ORIGIN=")
    Assert-Check -Name "Backend .env defines JWT_SECRET" -Condition ($content -match "JWT_SECRET=")
}

# 1.3 Frontend .env.example
$frontendEnvPath = Join-Path $PSScriptRoot "..\frontend\.env.example"
Assert-Check -Name "Frontend .env.example exists" -Condition (Test-Path $frontendEnvPath)
if (Test-Path $frontendEnvPath) {
    $content = Get-Content $frontendEnvPath -Raw
    Assert-Check -Name "Frontend .env defines VITE_API_BASE_URL" -Condition ($content -match "VITE_API_BASE_URL=")
    Assert-Check -Name "Frontend .env defines VITE_USE_MOCK" -Condition ($content -match "VITE_USE_MOCK=")
}

Write-Host "`n=== 2. Checking Docker Compose & Network Topology ===" -ForegroundColor Cyan

$dockerComposePath = Join-Path $PSScriptRoot "..\docker-compose.yml"
Assert-Check -Name "Root docker-compose.yml exists" -Condition (Test-Path $dockerComposePath)
if (Test-Path $dockerComposePath) {
    $compose = Get-Content $dockerComposePath -Raw
    Assert-Check -Name "Compose defines mysql service" -Condition ($compose -match "mysql:")
    Assert-Check -Name "Compose defines backend service" -Condition ($compose -match "backend:")
    Assert-Check -Name "Compose defines frontend service" -Condition ($compose -match "frontend:")
    Assert-Check -Name "Compose defines shared gym-network" -Condition ($compose -match "gym-network:")
    Assert-Check -Name "Frontend maps to FRONTEND_PORT (default 3000)" -Condition ($compose -match "\$\{FRONTEND_PORT:-3000\}:80")
}

$nginxConfPath = Join-Path $PSScriptRoot "..\frontend\nginx.conf"
Assert-Check -Name "frontend/nginx.conf exists" -Condition (Test-Path $nginxConfPath)
if (Test-Path $nginxConfPath) {
    $nginx = Get-Content $nginxConfPath -Raw
    Assert-Check -Name "Nginx proxies /api to backend container" -Condition ($nginx -match 'proxy_pass\s+\$backend_upstream' -or $nginx -match 'proxy_pass\s+http://backend:8080')
    Assert-Check -Name "Nginx includes Docker DNS resolver 127.0.0.11" -Condition ($nginx -match 'resolver 127\.0\.0\.11')
}

Write-Host "`n=== 3. Checking CORS Configuration in Backend ===" -ForegroundColor Cyan

$corsConfigPath = Join-Path $PSScriptRoot "..\backend\src\main\java\com\example\GYM_management_api\config\CorsConfig.java"
Assert-Check -Name "CorsConfig.java exists" -Condition (Test-Path $corsConfigPath)
if (Test-Path $corsConfigPath) {
    $cors = Get-Content $corsConfigPath -Raw
    Assert-Check -Name "CorsConfig parses comma-separated origins" -Condition ($cors -match '\.split\("\,"\)')
    Assert-Check -Name "CorsConfig uses allowedOriginPatterns" -Condition ($cors -match 'allowedOriginPatterns')
    Assert-Check -Name "CorsConfig enables allowCredentials" -Condition ($cors -match 'allowCredentials\(true\)')
}

Write-Host "`n=== 4. Checking Frontend Chặng 4 API Alignment ===" -ForegroundColor Cyan

# 4.1 api.js
$apiJsPath = Join-Path $PSScriptRoot "..\frontend\src\services\api.js"
if (Test-Path $apiJsPath) {
    $apiJs = Get-Content $apiJsPath -Raw
    Assert-Check -Name "USE_MOCK is controlled dynamically via VITE_USE_MOCK" -Condition ($apiJs -match "import\.meta\.env\.VITE_USE_MOCK === 'true'")
}

# 4.2 promotionService.js
$promoJsPath = Join-Path $PSScriptRoot "..\frontend\src\services\promotionService.js"
if (Test-Path $promoJsPath) {
    $promoJs = Get-Content $promoJsPath -Raw
    Assert-Check -Name "promotionService calls /admin/promotions" -Condition ($promoJs -match "/admin/promotions")
    Assert-Check -Name "promotionService calls /promotions/apply" -Condition ($promoJs -match "/promotions/apply")
}

# 4.3 staffService.js
$staffJsPath = Join-Path $PSScriptRoot "..\frontend\src\services\staffService.js"
if (Test-Path $staffJsPath) {
    $staffJs = Get-Content $staffJsPath -Raw
    Assert-Check -Name "staffService calls /admin/staffs with keyword" -Condition ($staffJs -match "keyword:")
    Assert-Check -Name "staffService calls /toggle-status" -Condition ($staffJs -match "toggle-status")
}

# 4.4 reportService.js
$reportJsPath = Join-Path $PSScriptRoot "..\frontend\src\services\reportService.js"
if (Test-Path $reportJsPath) {
    $reportJs = Get-Content $reportJsPath -Raw
    Assert-Check -Name "reportService calls /admin/reports/revenue" -Condition ($reportJs -match "/admin/reports/revenue")
}

Write-Host "`n=== SUMMARY ===" -ForegroundColor Cyan
Write-Host "Total Passed: $passes" -ForegroundColor Green
Write-Host "Total Failed: $failures" -ForegroundColor $(if ($failures -eq 0) { "Green" } else { "Red" })

if ($failures -gt 0) {
    exit 1
} else {
    Write-Host "`nAll SUB-2 verification checks passed successfully!" -ForegroundColor Green
    exit 0
}
