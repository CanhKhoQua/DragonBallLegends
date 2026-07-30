@echo off
setlocal EnableExtensions

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Build-OfflinePack.ps1" %*
exit /b %ERRORLEVEL%
