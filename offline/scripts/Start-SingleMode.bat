@echo off
setlocal EnableExtensions
title DragonBallLegends Offline - Single Mode

set "ROOT=%~dp0.."
for %%I in ("%ROOT%") do set "ROOT=%%~fI"

call :reset_client_profile "%USERPROFILE%\AppData\LocalLow\NRO\DragonBall"
call :reset_client_profile "%USERPROFILE%\AppData\LocalLow\NRO\DragonBallLegends"

copy /Y "%ROOT%\config\config.single.properties" "%ROOT%\server\data\config\config.properties" >nul
call "%ROOT%\scripts\_start_runtime.bat" single
if errorlevel 1 goto :fail

call "%ROOT%\scripts\_start_client.bat"
exit /b 0

:fail
echo.
echo Single Mode failed to start.
pause
exit /b 1

:reset_client_profile
set "PROFILE_DIR=%~1"
if not exist "%PROFILE_DIR%" exit /b 0
for %%F in (NRlink2 svselect acc pass userAo0 userAo1 userAo2 userAo3) do (
  if exist "%PROFILE_DIR%\%%F" del /F /Q "%PROFILE_DIR%\%%F" >nul 2>nul
)
exit /b 0
