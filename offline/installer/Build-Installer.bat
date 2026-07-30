@echo off
setlocal

set "ISCC=iscc"
where iscc >nul 2>nul
if errorlevel 1 set "ISCC=%LOCALAPPDATA%\Programs\Inno Setup 6\ISCC.exe"
if not exist "%ISCC%" set "ISCC=%ProgramFiles(x86)%\Inno Setup 6\ISCC.exe"
if not exist "%ISCC%" set "ISCC=%ProgramFiles%\Inno Setup 6\ISCC.exe"

if not exist "%ISCC%" (
  echo Inno Setup compiler was not found.
  echo Install Inno Setup, then run this file again.
  exit /b 1
)

"%ISCC%" "%~dp0DragonBallLegends.iss"
