@echo off
setlocal
cd /d "%~dp0"
where javaw.exe >nul 2>nul
if errorlevel 1 (
    echo Java 17 or newer is required. Install Java and add its bin directory to PATH.
    pause
    exit /b 1
)
start "" javaw.exe -jar "%~dp0app\ArmwrestlingTournament.jar"
