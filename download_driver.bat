@echo off
cd /d "%~dp0"
echo [HMS] Downloading official MySQL Connector/J driver into lib/...

if not exist lib mkdir lib

powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Write-Host 'Downloading mysql-connector-j-8.3.0.jar from Maven Central...'; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar' -OutFile 'lib\mysql-connector-j-8.3.0.jar' -UseBasicParsing; Write-Host 'Download complete!'"

if exist "lib\mysql-connector-j-8.3.0.jar" (
    echo [SUCCESS] MySQL JDBC Driver successfully saved to lib\mysql-connector-j-8.3.0.jar!
    echo You can now run run.bat or run_web.bat with full database connectivity.
) else (
    echo [ERROR] Download failed. Please ensure you are connected to the internet.
)
pause
