@echo off
REM Change current directory to the folder containing this batch script
cd /d "%~dp0"

REM ============================================================================
REM HOSPITAL MANAGEMENT SYSTEM - WEB APPLICATION SERVER LAUNCHER
REM Starts the built-in HTTP server on http://localhost:8080
REM ============================================================================

set JAVA_CMD=java
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\BlueJ\jdk\bin\java.exe" (
        set "JAVA_CMD=C:\Program Files\BlueJ\jdk\bin\java.exe"
    ) else (
        echo [ERROR] 'java' runtime not found on PATH.
        echo Please ensure Java 17+ JRE/JDK is installed.
        pause
        exit /b 1
    )
)

if not exist "bin\com\hospital\web\WebServer.class" (
    echo [HMS] Building application before launch...
    call "%~dp0build.bat"
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build failed.
        pause
        exit /b 1
    )
)

echo.
echo ================================================================================
echo   STARTING HOSPITAL MANAGEMENT SYSTEM WEB SERVER
echo ================================================================================
echo   Opening in your web browser: http://localhost:8080/
echo   Connecting to MySQL via JDBC...
echo ================================================================================
echo.

REM Automatically open default browser after starting
start http://localhost:8080/

set CP=bin
if exist "lib\*.jar" (
    set CP=bin;lib\*
)

"%JAVA_CMD%" -cp "%CP%" com.hospital.web.WebServer 8080
if %ERRORLEVEL% NEQ 0 (
    pause
)
