@echo off
setlocal EnableExtensions

set "ROOT=%~dp0.."
for %%I in ("%ROOT%") do set "ROOT=%%~fI"
set "CLIENT_DIR=%ROOT%\client"

if not exist "%CLIENT_DIR%" (
  echo Client folder is missing: "%CLIENT_DIR%"
  exit /b 1
)

set "CLIENT_EXE="
for %%F in ("%CLIENT_DIR%\*.exe") do (
  if /I not "%%~nxF"=="UnityCrashHandler64.exe" if /I not "%%~nxF"=="UnityCrashHandler32.exe" (
    set "CLIENT_EXE=%%~fF"
    goto :found
  )
)

:found
if not defined CLIENT_EXE (
  echo No client exe found in "%CLIENT_DIR%".
  exit /b 1
)

start "" "%CLIENT_EXE%"
exit /b 0
