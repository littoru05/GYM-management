@echo off
setlocal
title Gym Management - Extract Transaction Logs

echo ===============================================================================
echo  [LOG AUDIT] Trich xuat Log Giao dich va Rollback phuc vu Tester Doi soat
echo ===============================================================================

set "SCRIPT_DIR=%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%extract-transaction-logs.ps1" %*
set "EXIT_CODE=%ERRORLEVEL%"

if %EXIT_CODE% EQU 0 (
    echo.
    echo [DONE] Trich xuat log hoan tat. File log da duoc luu trong docs/testing/reports/logs/
) else (
    echo.
    echo [ERROR] Trich xuat log that bai voi ma loi: %EXIT_CODE%
)

exit /b %EXIT_CODE%
