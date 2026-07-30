@echo off
setlocal EnableExtensions
title DragonBallLegends Offline - Host LAN

set "ROOT=%~dp0.."
for %%I in ("%ROOT%") do set "ROOT=%%~fI"

copy /Y "%ROOT%\config\config.host-lan.properties" "%ROOT%\server\data\config\config.properties" >nul

echo.
echo LAN addresses friends can try:
for /f "delims=" %%A in ('powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.NetworkInformation.NetworkInterface]::GetAllNetworkInterfaces() | Where-Object { $_.OperationalStatus -eq [Net.NetworkInformation.OperationalStatus]::Up } | ForEach-Object { $_.GetIPProperties().UnicastAddresses } | Where-Object { $_.Address.AddressFamily -eq [Net.Sockets.AddressFamily]::InterNetwork -and -not [Net.IPAddress]::IsLoopback($_.Address) } | ForEach-Object { $_.Address.IPAddressToString } | Select-Object -Unique"') do (
  if not defined LAN_IP set "LAN_IP=%%A"
  echo   %%A:14445
)
if defined LAN_IP (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$path = Join-Path $env:ROOT 'server\data\config\config.properties'; $replacement = 'server.sv1              = DragonBallLegends LAN:' + $env:LAN_IP + ':14445:0,0,0'; $lines = Get-Content -LiteralPath $path; $done = $false; $out = foreach ($line in $lines) { if (-not $done -and $line.TrimStart().StartsWith('server.sv1')) { $done = $true; $replacement } else { $line } }; if (-not $done) { $out += $replacement }; Set-Content -LiteralPath $path -Encoding UTF8 -Value $out"
) else (
  echo   not detected - check ipconfig and use port 14445
)
echo.

call "%ROOT%\scripts\_start_runtime.bat" host-lan
if errorlevel 1 goto :fail

call "%ROOT%\scripts\_start_client.bat"
exit /b 0

:fail
echo.
echo Host LAN failed to start.
pause
exit /b 1
