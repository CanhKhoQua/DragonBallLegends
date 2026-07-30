@echo off
setlocal EnableExtensions
title DragonBallLegends Offline - Backup DefaultWorld

set "ROOT=%~dp0.."
for %%I in ("%ROOT%") do set "ROOT=%%~fI"
set "WORLD_DIR=%ROOT%\saves\DefaultWorld"
set "BACKUP_DIR=%ROOT%\saves\backups"

if not exist "%WORLD_DIR%" (
  echo DefaultWorld save does not exist yet.
  pause
  exit /b 1
)

echo Stop the server/database before backing up to avoid a partial database copy.
set /P "CONFIRM=Continue backup now? Type YES: "
if /I not "%CONFIRM%"=="YES" exit /b 1

if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%" >nul 2>nul
for /f %%T in ('powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-Date -Format yyyyMMdd-HHmmss"') do set "STAMP=%%T"
set "BACKUP_FILE=%BACKUP_DIR%\DefaultWorld-%STAMP%.zip"

powershell -NoProfile -ExecutionPolicy Bypass -Command "Compress-Archive -LiteralPath '%WORLD_DIR%' -DestinationPath '%BACKUP_FILE%' -Force"
if errorlevel 1 (
  echo Backup failed.
  pause
  exit /b 1
)

echo Backup created:
echo   %BACKUP_FILE%
pause
exit /b 0
