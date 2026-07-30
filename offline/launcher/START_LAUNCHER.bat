@echo off
setlocal EnableExtensions
set "ROOT=%~dp0.."
powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT%\launcher\DragonBallLegendsLauncher.ps1"
