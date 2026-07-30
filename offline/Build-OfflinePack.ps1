param(
    [string]$SourceServerPath = "",
    [string]$SourceClientPath = "",
    [string]$ClientBuildPath = "",
    [string]$SourceJavaPath = "",
    [string]$OutputPath = "",
    [switch]$BuildUnityClient
)

$ErrorActionPreference = "Stop"

function Copy-DirectoryContents {
    param([string]$From, [string]$To)
    if (!(Test-Path $To)) { New-Item -ItemType Directory -Path $To | Out-Null }
    Copy-Item -Path (Join-Path $From "*") -Destination $To -Recurse -Force
}

function Assert-SafeOutputPath {
    param([string]$Path)

    $full = [System.IO.Path]::GetFullPath($Path).TrimEnd('\')
    $root = [System.IO.Path]::GetPathRoot($full).TrimEnd('\')
    if ($full -eq $root) {
        throw "Refusing to use drive root as output path: $full"
    }
    if ([string]::IsNullOrWhiteSpace([System.IO.Path]::GetFileName($full))) {
        throw "Refusing unsafe output path: $full"
    }
}

function Find-UnityExe {
    $candidates = @(
        "C:\Program Files\Unity\Hub\Editor\6000.3.18f1\Editor\Unity.exe",
        "C:\Program Files\Unity\Hub\Editor\6000.3.10f1\Editor\Unity.exe"
    )
    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) { return $candidate }
    }
    $found = Get-ChildItem "C:\Program Files\Unity\Hub\Editor" -Recurse -Filter Unity.exe -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) { return $found.FullName }
    return $null
}

function Copy-PortableMysql {
    param([string]$RuntimeDir)

    $xampp = "C:\xampp\mysql"
    if (!(Test-Path $xampp)) {
        Set-Content -LiteralPath (Join-Path $RuntimeDir "PUT_MYSQL_HERE.txt") -Encoding UTF8 -Value @"
Portable MySQL/MariaDB runtime is missing.

Expected layout:
  runtime\mysql\bin\mysqld.exe
  runtime\mysql\bin\mysql.exe
  runtime\mysql\data_template\
"@
        return
    }

    $mysqlDir = Join-Path $RuntimeDir "mysql"
    New-Item -ItemType Directory -Path $mysqlDir | Out-Null
    foreach ($name in @("bin", "share", "scripts", "COPYING", "CREDITS", "THIRDPARTY", "README.md")) {
        $src = Join-Path $xampp $name
        if (Test-Path $src) {
            Copy-Item -LiteralPath $src -Destination $mysqlDir -Recurse -Force
        }
    }
    if (Test-Path (Join-Path $xampp "backup")) {
        Copy-Item -LiteralPath (Join-Path $xampp "backup") -Destination (Join-Path $mysqlDir "data_template") -Recurse -Force
    }
}

function Publish-NativeLauncher {
    param([string]$Repo)

    $project = Join-Path $Repo "offline\launcher-native\DragonBallLegends.Launcher\DragonBallLegends.Launcher.csproj"
    $publishDir = Join-Path $Repo "offline\launcher-native\publish"
    $exe = Join-Path $publishDir "DragonBallLegends Launcher.exe"
    if (!(Test-Path $project)) { return $null }
    $dotnet = Get-Command dotnet -ErrorAction SilentlyContinue
    if (!$dotnet) { return $null }

    & dotnet publish $project -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -p:IncludeNativeLibrariesForSelfExtract=true -o $publishDir
    if ($LASTEXITCODE -ne 0 -or !(Test-Path $exe)) {
        Write-Warning "Native launcher build failed. Pack will keep the PowerShell launcher fallback."
        return $null
    }
    return $exe
}

function Copy-JavaRuntime {
    param([string]$From, [string]$RuntimeDir)

    if (!$From) { return }
    if (!(Test-Path $From)) { throw "Java runtime path not found: $From" }

    $javaExe = Join-Path $From "bin\java.exe"
    if (!(Test-Path $javaExe)) {
        throw "Java runtime path must contain bin\java.exe: $From"
    }

    $target = Join-Path $RuntimeDir "java"
    if (Test-Path $target) {
        Remove-Item -LiteralPath $target -Recurse -Force
    }
    Copy-Item -LiteralPath $From -Destination $target -Recurse -Force
}

function Write-BuildInfo {
    param([string]$Pack)

    $serverJar = Join-Path $Pack "server\Server.jar"
    $launcher = Join-Path $Pack "DragonBallLegends Launcher.exe"
    $java = Join-Path $Pack "runtime\java\bin\java.exe"
    $lines = @(
        "name=DragonBallLegends",
        "version=1.0.0",
        "built_utc=$((Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ"))",
        "server_jar_sha256=$(if (Test-Path $serverJar) { (Get-FileHash -Algorithm SHA256 -LiteralPath $serverJar).Hash.ToLowerInvariant() } else { "missing" })",
        "launcher_sha256=$(if (Test-Path $launcher) { (Get-FileHash -Algorithm SHA256 -LiteralPath $launcher).Hash.ToLowerInvariant() } else { "missing" })",
        "bundled_java=$(if (Test-Path $java) { "yes" } else { "no" })"
    )
    Set-Content -LiteralPath (Join-Path $Pack "build_info.txt") -Encoding UTF8 -Value $lines
}

function Write-InstallManifest {
    param([string]$Pack)

    $packFull = (Resolve-Path $Pack).Path.TrimEnd('\')
    $manifest = Join-Path $Pack "install_manifest.tsv"
    if (Test-Path $manifest) { Remove-Item -LiteralPath $manifest -Force }

    $rows = New-Object System.Collections.Generic.List[string]
    $rows.Add("# sha256`tbytes`trelative_path")
    Get-ChildItem -LiteralPath $Pack -File -Recurse | ForEach-Object {
        $rel = $_.FullName.Substring($packFull.Length + 1).Replace('\', '/')
        if ($rel -eq "install_manifest.tsv") { return }
        if ($rel -eq "server/data/config/config.properties") { return }
        if ($rel -like "server/log/*") { return }
        if ($rel -like "saves/*") { return }
        if ($rel -like "logs/*") { return }
        if ($rel -eq "client_build.log") { return }
        if ($rel -eq "runtime/.db_initialized") { return }
        if ($rel -eq "runtime/offline-db.pid") { return }
        if ($rel -eq "runtime/offline-server.pid") { return }
        if ($rel -eq "runtime/offline-world.txt") { return }
        if ($rel -like "runtime/dbdata/*") { return }
        $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $_.FullName).Hash.ToLowerInvariant()
        $rows.Add("$hash`t$($_.Length)`t$rel")
    }
    Set-Content -LiteralPath $manifest -Encoding UTF8 -Value $rows
}

function Clear-GeneratedRuntimeState {
    param([string]$Pack)

    $paths = @(
        "logs",
        "server\log",
        "runtime\dbdata",
        "runtime\.db_initialized",
        "runtime\offline-db.pid",
        "runtime\offline-server.pid",
        "runtime\offline-world.txt",
        "client_build.log"
    )
    foreach ($rel in $paths) {
        $path = Join-Path $Pack $rel
        if (Test-Path $path) {
            Remove-Item -LiteralPath $path -Recurse -Force
        }
    }

    $saves = Join-Path $Pack "saves"
    if (Test-Path $saves) {
        Get-ChildItem -LiteralPath $saves -Force | Remove-Item -Recurse -Force
    } else {
        New-Item -ItemType Directory -Path $saves | Out-Null
    }
}

$repo = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
if (!$SourceServerPath) { $SourceServerPath = $repo }
if (!$OutputPath) { $OutputPath = Join-Path (Split-Path $repo -Parent) "DragonBallLegends_OfflinePack" }
$pack = $OutputPath
Assert-SafeOutputPath $pack

if (Test-Path $pack) {
    Remove-Item -LiteralPath $pack -Recurse -Force
}

New-Item -ItemType Directory -Path $pack | Out-Null
foreach ($dir in @("server", "server\data\config", "database", "runtime", "saves", "client", "config", "scripts", "launcher")) {
    New-Item -ItemType Directory -Path (Join-Path $pack $dir) | Out-Null
}

$serverJar = Join-Path $SourceServerPath "Server.jar"
if (!(Test-Path $serverJar)) {
    $serverJar = Join-Path $SourceServerPath "dist\DragonBallLegends.jar"
}
if (!(Test-Path $serverJar)) {
    throw "Server jar not found. Build the server first or pass -SourceServerPath."
}
Copy-Item -LiteralPath $serverJar -Destination (Join-Path $pack "server\Server.jar") -Force

Copy-Item -LiteralPath (Join-Path $SourceServerPath "lib") -Destination (Join-Path $pack "server\lib") -Recurse -Force
Get-ChildItem -LiteralPath (Join-Path $SourceServerPath "data") -Force | Where-Object { $_.Name -ne "config" } | ForEach-Object {
    Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $pack "server\data") -Recurse -Force
}

Copy-Item -LiteralPath (Join-Path $repo "offline\config\config.single.properties") -Destination (Join-Path $pack "server\data\config\config.properties") -Force
Copy-DirectoryContents (Join-Path $repo "offline\config") (Join-Path $pack "config")
Copy-DirectoryContents (Join-Path $repo "offline\scripts") (Join-Path $pack "scripts")
Copy-DirectoryContents (Join-Path $repo "offline\launcher") (Join-Path $pack "launcher")

$nativeLauncher = Publish-NativeLauncher $repo
if ($nativeLauncher -and (Test-Path $nativeLauncher)) {
    Copy-Item -LiteralPath $nativeLauncher -Destination (Join-Path $pack "DragonBallLegends Launcher.exe") -Force
}

$dumpCandidates = @(
    (Join-Path $SourceServerPath "database\backup.sql"),
    (Join-Path $SourceServerPath "database\nroserver.sql"),
    (Join-Path $repo "database\backup.sql"),
    (Join-Path $repo "database\nroserver.sql")
)
$databaseDump = $dumpCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (!$databaseDump) {
    throw "Database dump not found. Expected database\backup.sql or database\nroserver.sql."
}
node (Join-Path $repo "offline\tools\generate_offline_seed.mjs") $databaseDump (Join-Path $pack "database\offline_seed.sql")

$migrationsSource = Join-Path $SourceServerPath "database\migrations"
if (Test-Path $migrationsSource) {
    Copy-DirectoryContents $migrationsSource (Join-Path $pack "database\migrations")
} else {
    New-Item -ItemType Directory -Path (Join-Path $pack "database\migrations") | Out-Null
    Set-Content -LiteralPath (Join-Path $pack "database\migrations\README.txt") -Encoding UTF8 -Value "Put future offline DB migration .sql files in this folder."
}

Copy-PortableMysql (Join-Path $pack "runtime")
Copy-JavaRuntime $SourceJavaPath (Join-Path $pack "runtime")

if ($ClientBuildPath -and (Test-Path $ClientBuildPath)) {
    Copy-DirectoryContents $ClientBuildPath (Join-Path $pack "client")
} elseif ($BuildUnityClient) {
    $unity = Find-UnityExe
    if (!$unity) { throw "Unity.exe not found. Pass -ClientBuildPath instead." }
    if (!$SourceClientPath) { throw "Pass -SourceClientPath when using -BuildUnityClient." }
    if (!(Test-Path $SourceClientPath)) { throw "Client source not found: $SourceClientPath" }
    $clientExe = Join-Path $pack "client\DragonBallLegends.exe"
    $log = Join-Path $pack "client_build.log"
    & $unity -batchmode -quit -projectPath $SourceClientPath -buildWindows64Player $clientExe -logFile $log
    if ($LASTEXITCODE -ne 0 -or !(Test-Path $clientExe)) {
        throw "Unity client build failed. See $log"
    }
} else {
    Set-Content -LiteralPath (Join-Path $pack "client\PUT_CLIENT_BUILD_HERE.txt") -Encoding UTF8 -Value @"
Put the built Windows Unity client files in this folder.

The folder should contain:
  DragonBallLegends.exe
  DragonBallLegends_Data\

Or rebuild the pack with:
  offline\Build-OfflinePack.bat -ClientBuildPath "D:\path\to\client-build"
  offline\Build-OfflinePack.bat -BuildUnityClient
"@
}

Set-Content -LiteralPath (Join-Path $pack "START_SINGLE_MODE.bat") -Encoding ASCII -Value '@echo off
call "%~dp0scripts\Start-SingleMode.bat"
'
Set-Content -LiteralPath (Join-Path $pack "START_LAUNCHER.bat") -Encoding ASCII -Value '@echo off
if exist "%~dp0DragonBallLegends Launcher.exe" (
  start "" "%~dp0DragonBallLegends Launcher.exe"
  exit /b 0
)
call "%~dp0launcher\START_LAUNCHER.bat"
'
Set-Content -LiteralPath (Join-Path $pack "START_HOST_LAN.bat") -Encoding ASCII -Value '@echo off
call "%~dp0scripts\Start-HostLAN.bat"
'
Set-Content -LiteralPath (Join-Path $pack "STOP_OFFLINE.bat") -Encoding ASCII -Value '@echo off
call "%~dp0scripts\Stop-Offline.bat"
'
Set-Content -LiteralPath (Join-Path $pack "BACKUP_DEFAULT_WORLD.bat") -Encoding ASCII -Value '@echo off
call "%~dp0scripts\Backup-DefaultWorld.bat"
'
Set-Content -LiteralPath (Join-Path $pack "RESET_DEFAULT_WORLD.bat") -Encoding ASCII -Value '@echo off
call "%~dp0scripts\Reset-DefaultWorld.bat"
'

Set-Content -LiteralPath (Join-Path $pack "README_OFFLINE.txt") -Encoding UTF8 -Value @"
DragonBallLegends Offline Pack

Launcher:
  DragonBallLegends Launcher.exe
  START_LAUNCHER.bat

Single player:
  START_SINGLE_MODE.bat

Host LAN:
  START_HOST_LAN.bat

Stop server/database:
  STOP_OFFLINE.bat

Backup/reset local save:
  BACKUP_DEFAULT_WORLD.bat
  RESET_DEFAULT_WORLD.bat

Ports:
  Game server: 14445
  Local DB: 3307

Demo login:
  Username: offline
  Password: 123456

Notes:
  - Use DragonBallLegends Launcher.exe or the top-level START_LAUNCHER.bat for the full launcher UI.
  - First start imports database\offline_seed.sql automatically.
  - Single mode resets old saved server/login selection so the client uses 127.0.0.1:14445.
  - The native launcher supports multiple local worlds. Each world stores account/player data in saves\<WorldName>\dbdata.
  - Single Mode and Host LAN use the selected world in the native launcher.
  - DB patch files in database\migrations are applied once per world in filename order.
  - Join Friend writes the friend server address into the client profile, then opens the client.
  - Host LAN advertises your detected LAN IP on port 14445. Allow Windows Firewall when prompted.
  - Native launcher backup/restore/export/rename/delete tools operate on the selected world.
  - BACKUP_DEFAULT_WORLD.bat and RESET_DEFAULT_WORLD.bat are fallback batch tools for DefaultWorld.
  - Release packs should not include existing saves, runtime dbdata, pid files, or generated logs.
  - If runtime\java exists, the launcher uses it before Java installed on Windows.
  - Diagnostics checks Java, database runtime, client exe, server jar, seed SQL, config, and ports.
  - Verify Files checks install_manifest.tsv to detect missing or corrupted release files.
  - Copy Report copies launcher/server/database/config diagnostics to the clipboard.
  - Host LAN mode shows your LAN IP for friends.
"@

Write-BuildInfo $pack
Clear-GeneratedRuntimeState $pack
Write-InstallManifest $pack

Write-Host "Offline pack created:"
Write-Host "  $pack"
