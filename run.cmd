@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
    echo Java JDK 17 or newer is required. Make sure javac is on PATH.
    exit /b 1
)
if not exist out mkdir out
javac --release 17 -encoding UTF-8 -d out src\tournament\*.java
if errorlevel 1 exit /b 1
java -cp out tournament.Main
