@echo off
REM Change current directory to the folder containing this batch script
cd /d "%~dp0"

REM ============================================================================
REM HOSPITAL MANAGEMENT SYSTEM - BUILD SCRIPT
REM Compiles all Java source files into the bin/ directory
REM ============================================================================

echo [HMS] Building Hospital Management System...

REM Check if javac is on PATH, or try BlueJ JDK if present
set JAVAC_CMD=javac
where javac >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\BlueJ\jdk\bin\javac.exe" (
        set "JAVAC_CMD=C:\Program Files\BlueJ\jdk\bin\javac.exe"
    ) else (
        echo [ERROR] 'javac' compiler not found on PATH or in standard directory.
        echo Please ensure Java 17+ JDK is installed and JAVA_HOME/bin is in your PATH.
        pause
        exit /b 1
    )
)

if not exist bin mkdir bin

echo [HMS] Using compiler: %JAVAC_CMD%
echo [HMS] Compiling source files from src/main/java...

dir /s /B src\main\java\*.java > sources.txt
"%JAVAC_CMD%" -d bin -encoding UTF-8 @sources.txt
set COMPILE_STATUS=%ERRORLEVEL%
if exist sources.txt del sources.txt

if %COMPILE_STATUS% EQU 0 (
    echo [HMS] Copying resource files...
    if exist src\main\resources (
        xcopy /s /y /q src\main\resources\* bin\ >nul 2>nul
    )
    echo [HMS] Build SUCCESSFUL! Output classes generated in bin/
) else (
    echo [HMS] Build FAILED with error code %COMPILE_STATUS%
    pause
    exit /b %COMPILE_STATUS%
)
