@echo off
setlocal
title Gym Management - Reset Test Database

echo ===============================================================================
echo  [RESET DB] Khoi phuc va lam moi Database Test (Chang 4 va Demo Data)
echo ===============================================================================

set "SCRIPT_DIR=%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%reset-restore-db.ps1" %*
set "EXIT_CODE=%ERRORLEVEL%"

if %EXIT_CODE% EQU 0 (
    echo.
    echo [DONE] Lam moi Database thanh cong! Tester san sang chay kich ban test.
) else (
    echo.
    echo [ERROR] Reset Database that bai voi ma loi: %EXIT_CODE%
)

exit /b %EXIT_CODE%
