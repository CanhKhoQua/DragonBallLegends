@echo off
setlocal EnableExtensions
title DragonBallLegends Offline - Stop

set "ROOT=%~dp0.."
for %%I in ("%ROOT%") do set "ROOT=%%~fI"
set "DB_PORT=3307"
set "MYSQLADMIN="
if exist "%ROOT%\runtime\mariadb\bin\mysqladmin.exe" set "MYSQLADMIN=%ROOT%\runtime\mariadb\bin\mysqladmin.exe"
if not defined MYSQLADMIN if exist "%ROOT%\runtime\mysql\bin\mysqladmin.exe" set "MYSQLADMIN=%ROOT%\runtime\mysql\bin\mysqladmin.exe"

echo Stopping DragonBallLegends Offline processes...
if defined MYSQLADMIN if exist "%ROOT%\runtime\offline-db.pid" (
  "%MYSQLADMIN%" --host=127.0.0.1 --port=%DB_PORT% --user=root shutdown >nul 2>nul
)
call :kill_pid "%ROOT%\runtime\offline-server.pid"
call :kill_pid "%ROOT%\runtime\offline-db.pid"
if exist "%ROOT%\runtime\offline-world.txt" del /F /Q "%ROOT%\runtime\offline-world.txt" >nul 2>nul
echo Done.
pause
exit /b 0

:kill_pid
set "PID_FILE=%~1"
if not exist "%PID_FILE%" exit /b 0
set /p PID=<"%PID_FILE%"
taskkill /PID %PID% /T /F >nul 2>nul
del /F /Q "%PID_FILE%" >nul 2>nul
exit /b 0
