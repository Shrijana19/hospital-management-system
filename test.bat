@echo off
REM Change current directory to the folder containing this batch script
cd /d "%~dp0"

REM ============================================================================
REM HOSPITAL MANAGEMENT SYSTEM - TEST SUITE RUNNER SCRIPT
REM ============================================================================

set JAVA_CMD=java
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\BlueJ\jdk\bin\java.exe" (
        set "JAVA_CMD=C:\Program Files\BlueJ\jdk\bin\java.exe"
    )
)

echo [HMS] Running Standalone Automated Test Suite...
"%JAVA_CMD%" -cp bin com.hospital.test.StandaloneTestRunner
if %ERRORLEVEL% NEQ 0 (
    pause
)
