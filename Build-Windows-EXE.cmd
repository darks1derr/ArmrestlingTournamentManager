@echo off
setlocal
cd /d "%~dp0"
where jpackage.exe >nul 2>nul
if errorlevel 1 (
    echo A Windows JDK 17 or newer with jpackage on PATH is required.
    pause
    exit /b 1
)
if exist "dist\ArmwrestlingTournament" (
    echo Move or rename dist\ArmwrestlingTournament before rebuilding.
    pause
    exit /b 1
)
jpackage --type app-image --name ArmwrestlingTournament --input app --main-jar ArmwrestlingTournament.jar --dest dist
if errorlevel 1 (
    pause
    exit /b 1
)
echo Open dist\ArmwrestlingTournament\ArmwrestlingTournament.exe
pause
