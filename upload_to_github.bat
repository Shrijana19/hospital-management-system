@echo off
cd /d "%~dp0"

echo ============================================================================
echo   UPLOADING HOSPITAL MANAGEMENT SYSTEM TO GITHUB
echo   Repository: https://github.com/Shrijana19/hospital-management-system
echo ============================================================================
echo.

git push -u origin main

echo.
if %ERRORLEVEL% EQU 0 (
    echo ============================================================================
    echo   [SUCCESS] All code and README uploaded to GitHub!
    echo   Refresh your browser: https://github.com/Shrijana19/hospital-management-system
    echo ============================================================================
) else (
    echo [NOTE] If a browser window opened, click "Sign in with your browser" to approve.
)
echo.
pause
