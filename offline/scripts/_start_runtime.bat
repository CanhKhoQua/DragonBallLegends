@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0.."
for %%I in ("%ROOT%") do set "ROOT=%%~fI"
set "MODE=%~1"
set "DB_PORT=3307"
set "GAME_PORT=14445"
set "WORLD_NAME=DefaultWorld"
set "WORLD_DIR=%ROOT%\saves\%WORLD_NAME%"

set "MYSQLD="
set "MYSQL="
set "JAVA_EXE=java"
if exist "%ROOT%\runtime\mariadb\bin\mariadbd.exe" set "MYSQLD=%ROOT%\runtime\mariadb\bin\mariadbd.exe"
if exist "%ROOT%\runtime\mariadb\bin\mysql.exe" set "MYSQL=%ROOT%\runtime\mariadb\bin\mysql.exe"
if not defined MYSQLD if exist "%ROOT%\runtime\mysql\bin\mysqld.exe" set "MYSQLD=%ROOT%\runtime\mysql\bin\mysqld.exe"
if not defined MYSQL if exist "%ROOT%\runtime\mysql\bin\mysql.exe" set "MYSQL=%ROOT%\runtime\mysql\bin\mysql.exe"
if exist "%ROOT%\runtime\java\bin\java.exe" set "JAVA_EXE=%ROOT%\runtime\java\bin\java.exe"

if not defined MYSQLD (
  echo Missing MariaDB/MySQL portable runtime.
  echo Put MariaDB under "%ROOT%\runtime\mariadb" or "%ROOT%\runtime\mysql".
  exit /b 1
)
if not defined MYSQL (
  echo Missing mysql.exe client in portable runtime.
  exit /b 1
)

if not exist "%ROOT%\runtime" mkdir "%ROOT%\runtime" >nul 2>nul
if not exist "%ROOT%\saves" mkdir "%ROOT%\saves" >nul 2>nul
if not exist "%WORLD_DIR%" mkdir "%WORLD_DIR%" >nul 2>nul

set "DB_DATA=%WORLD_DIR%\dbdata"
if not exist "%DB_DATA%" if exist "%ROOT%\runtime\dbdata" (
  echo Migrating legacy save to saves\%WORLD_NAME%...
  xcopy /E /I /Y "%ROOT%\runtime\dbdata" "%DB_DATA%" >nul
  if exist "%ROOT%\runtime\.db_initialized" copy /Y "%ROOT%\runtime\.db_initialized" "%WORLD_DIR%\.db_initialized" >nul
)

if not exist "%DB_DATA%" (
  if exist "%ROOT%\runtime\mariadb\data_template" (
    xcopy /E /I /Y "%ROOT%\runtime\mariadb\data_template" "%DB_DATA%" >nul
  ) else if exist "%ROOT%\runtime\mysql\data_template" (
    xcopy /E /I /Y "%ROOT%\runtime\mysql\data_template" "%DB_DATA%" >nul
  ) else (
    mkdir "%DB_DATA%" >nul 2>nul
    "%MYSQLD%" --initialize-insecure --datadir="%DB_DATA%" >nul 2>nul
  )
)

call :port_open %DB_PORT%
if not errorlevel 1 (
  call :pid_alive "%ROOT%\runtime\offline-db.pid"
  if errorlevel 1 (
    echo Port %DB_PORT% is already in use by another process.
    echo Close it before starting DragonBallLegends Offline.
    exit /b 1
  )
  if exist "%ROOT%\runtime\offline-world.txt" (
    set /p ACTIVE_WORLD=<"%ROOT%\runtime\offline-world.txt"
    if /I not "!ACTIVE_WORLD!"=="%WORLD_NAME%" (
      echo Database is already running for !ACTIVE_WORLD!.
      echo Stop it before starting %WORLD_NAME%.
      exit /b 1
    )
  )
  echo Database already running.
  goto :db_ready
)

start "DragonBallLegends Offline DB" /MIN "%MYSQLD%" --no-defaults --datadir="%DB_DATA%" --port=%DB_PORT% --bind-address=127.0.0.1 --pid-file="%ROOT%\runtime\offline-db.pid" --console
echo %WORLD_NAME%>"%ROOT%\runtime\offline-world.txt"

echo Waiting for database...
for /L %%I in (1,1,40) do (
  "%MYSQL%" --host=127.0.0.1 --port=%DB_PORT% --user=root --execute="SELECT 1" >nul 2>nul
  if not errorlevel 1 goto :db_ready
  timeout /t 1 /nobreak >nul
)
echo Database did not start on port %DB_PORT%.
exit /b 1

:db_ready
if not exist "%WORLD_DIR%\.db_initialized" (
  echo Importing offline database seed...
  "%MYSQL%" --host=127.0.0.1 --port=%DB_PORT% --user=root --execute="CREATE DATABASE IF NOT EXISTS nroserver CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;" || exit /b 1
  set "SEED_FILE=%ROOT%\database\offline_seed.sql"
  set "SEED_FILE=!SEED_FILE:\=/!"
  "%MYSQL%" --host=127.0.0.1 --port=%DB_PORT% --user=root --default-character-set=utf8mb4 --database=nroserver --execute="source !SEED_FILE!"
  if errorlevel 1 exit /b 1
  echo initialized>"%WORLD_DIR%\.db_initialized"
)

call :apply_migrations
if errorlevel 1 exit /b 1

echo Starting server...
call :port_open %GAME_PORT%
if not errorlevel 1 (
  call :pid_alive "%ROOT%\runtime\offline-server.pid"
  if errorlevel 1 (
    echo Port %GAME_PORT% is already in use by another server.
    echo Stop it before starting DragonBallLegends Offline.
    exit /b 1
  )
  echo Server already running.
  exit /b 0
)

for /f %%P in ('powershell -NoProfile -ExecutionPolicy Bypass -Command "$p = Start-Process -FilePath '%JAVA_EXE%' -ArgumentList '-server','-Dfile.encoding=UTF-8','-jar','Server.jar' -WorkingDirectory '%ROOT%\server' -WindowStyle Minimized -PassThru; $p.Id"') do set "SERVER_PID=%%P"
if defined SERVER_PID echo !SERVER_PID!>"%ROOT%\runtime\offline-server.pid"

echo Waiting for game server...
for /L %%I in (1,1,60) do (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$c = New-Object Net.Sockets.TcpClient; try { $c.Connect('127.0.0.1', %GAME_PORT%); $c.Close(); exit 0 } catch { exit 1 }" >nul 2>nul
  if not errorlevel 1 exit /b 0
  timeout /t 1 /nobreak >nul
)

echo Game server did not start on port %GAME_PORT%.
exit /b 1

:port_open
powershell -NoProfile -ExecutionPolicy Bypass -Command "$c = New-Object Net.Sockets.TcpClient; try { $iar = $c.BeginConnect('127.0.0.1', %~1, $null, $null); if (-not $iar.AsyncWaitHandle.WaitOne(300, $false)) { exit 1 }; $c.EndConnect($iar); exit 0 } catch { exit 1 } finally { $c.Close() }" >nul 2>nul
exit /b %errorlevel%

:pid_alive
set "PID_FILE=%~1"
if not exist "%PID_FILE%" exit /b 1
set /p PID=<"%PID_FILE%"
tasklist /FI "PID eq %PID%" | findstr /R /C:"[ ]%PID% " >nul 2>nul
exit /b %errorlevel%

:apply_migrations
if not exist "%ROOT%\database\migrations\*.sql" exit /b 0
if not exist "%WORLD_DIR%\.migrations" mkdir "%WORLD_DIR%\.migrations" >nul 2>nul
"%MYSQL%" --host=127.0.0.1 --port=%DB_PORT% --user=root --database=nroserver --execute="CREATE TABLE IF NOT EXISTS offline_schema_migrations (filename VARCHAR(255) NOT NULL PRIMARY KEY, checksum CHAR(64) NOT NULL, applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;" || exit /b 1
for %%M in ("%ROOT%\database\migrations\*.sql") do (
  if not exist "%WORLD_DIR%\.migrations\%%~nxM.applied" (
    echo Applying DB migration %%~nxM...
    set "MIGRATION_FILE=%%~fM"
    set "MIGRATION_FILE=!MIGRATION_FILE:\=/!"
    "%MYSQL%" --host=127.0.0.1 --port=%DB_PORT% --user=root --default-character-set=utf8mb4 --database=nroserver --execute="source !MIGRATION_FILE!" || exit /b 1
    "%MYSQL%" --host=127.0.0.1 --port=%DB_PORT% --user=root --database=nroserver --execute="REPLACE INTO offline_schema_migrations (filename, checksum) VALUES ('%%~nxM', 'batch-fallback');" || exit /b 1
    echo batch-fallback>"%WORLD_DIR%\.migrations\%%~nxM.applied"
  )
)
exit /b 0
