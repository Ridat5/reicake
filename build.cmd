@echo off
REM Root build script: builds the merged forge-port module and copies the jar to Desktop.
echo === Building merged forge-port module ===
cd /d "%~dp0forge-port"
call gradlew.bat build -x test
if errorlevel 1 (
    echo FAILED: forge-port
    exit /b 1
)
echo === Copying jar to Desktop ===
copy /y "%~dp0forge-port\build\libs\reiparticleskill-1.0-SNAPSHOT-forge-port.jar" "%USERPROFILE%\Desktop\"
echo === Done ===
