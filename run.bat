@echo off
REM Change current directory to the folder containing this batch script
cd /d "%~dp0"

REM ============================================================================
REM HOSPITAL MANAGEMENT SYSTEM - APPLICATION LAUNCH SCRIPT
REM Runs the desktop application from compiled bin/ directory
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

if not exist "bin\com\hospital\Main.class" (
    echo [HMS] Compiled classes not found. Running build first...
    call "%~dp0build.bat"
    if %ERRORLEVEL% NEQ 0 (
        echo [ERROR] Build failed. Cannot run application.
        pause
        exit /b 1
    )
)

echo [HMS] Launching Greendale Hospital Management System Desktop Application...
set CP=bin
if exist "lib\*.jar" (
    set CP=bin;lib\*
)

"%JAVA_CMD%" -cp "%CP%" com.hospital.Main
if %ERRORLEVEL% NEQ 0 (
    pause
)
