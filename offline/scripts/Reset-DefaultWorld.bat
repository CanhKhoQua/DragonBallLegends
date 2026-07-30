@echo off
setlocal EnableExtensions
title DragonBallLegends Offline - Reset DefaultWorld

set "ROOT=%~dp0.."
for %%I in ("%ROOT%") do set "ROOT=%%~fI"
set "WORLD_DIR=%ROOT%\saves\DefaultWorld"

echo This will delete the local DefaultWorld save.
echo Account/player data in this world will be reset on next start.
set /P "CONFIRM=Type RESET to continue: "
if /I not "%CONFIRM%"=="RESET" exit /b 1

call "%ROOT%\scripts\Stop-Offline.bat"

if exist "%WORLD_DIR%" rmdir /S /Q "%WORLD_DIR%"
echo DefaultWorld reset. Start Single Mode to import a fresh seed.
pause
exit /b 0
