Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

$ErrorActionPreference = "Stop"

$LauncherDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Root = (Resolve-Path (Join-Path $LauncherDir "..")).Path
$ScriptsDir = Join-Path $Root "scripts"
$WorldDir = Join-Path $Root "saves\DefaultWorld"
$ClientDir = Join-Path $Root "client"

function Start-Batch {
    param([string]$Name)
    $path = Join-Path $ScriptsDir $Name
    if (!(Test-Path $path)) {
        [System.Windows.Forms.MessageBox]::Show("Missing script:`r`n$path", "DragonBallLegends Launcher", "OK", "Error") | Out-Null
        return
    }
    Start-Process -FilePath $path -WorkingDirectory $Root
}

function Test-Port {
    param([int]$Port)
    $client = New-Object Net.Sockets.TcpClient
    try {
        $iar = $client.BeginConnect("127.0.0.1", $Port, $null, $null)
        if (!$iar.AsyncWaitHandle.WaitOne(250, $false)) { return $false }
        $client.EndConnect($iar)
        return $true
    } catch {
        return $false
    } finally {
        $client.Close()
    }
}

function Get-WorldStatus {
    if (Test-Path (Join-Path $WorldDir "dbdata")) {
        $items = Get-ChildItem -LiteralPath (Join-Path $WorldDir "dbdata") -ErrorAction SilentlyContinue
        if ($items) { return "DefaultWorld: Ready" }
    }
    return "DefaultWorld: Not created"
}

function Open-Folder {
    param([string]$Path)
    if (!(Test-Path $Path)) {
        New-Item -ItemType Directory -Path $Path | Out-Null
    }
    Start-Process explorer.exe -ArgumentList "`"$Path`""
}

$form = New-Object Windows.Forms.Form
$form.Text = "DragonBallLegends Launcher"
$form.StartPosition = "CenterScreen"
$form.ClientSize = New-Object Drawing.Size(520, 360)
$form.FormBorderStyle = "FixedSingle"
$form.MaximizeBox = $false
$form.BackColor = [Drawing.Color]::FromArgb(248, 249, 252)

$title = New-Object Windows.Forms.Label
$title.Text = "DragonBallLegends"
$title.Font = New-Object Drawing.Font("Segoe UI", 20, [Drawing.FontStyle]::Bold)
$title.AutoSize = $true
$title.Location = New-Object Drawing.Point(24, 18)
$form.Controls.Add($title)

$subtitle = New-Object Windows.Forms.Label
$subtitle.Text = "Offline launcher"
$subtitle.Font = New-Object Drawing.Font("Segoe UI", 10)
$subtitle.ForeColor = [Drawing.Color]::FromArgb(90, 96, 110)
$subtitle.AutoSize = $true
$subtitle.Location = New-Object Drawing.Point(28, 58)
$form.Controls.Add($subtitle)

$statusBox = New-Object Windows.Forms.GroupBox
$statusBox.Text = "Status"
$statusBox.Font = New-Object Drawing.Font("Segoe UI", 9, [Drawing.FontStyle]::Bold)
$statusBox.Location = New-Object Drawing.Point(24, 88)
$statusBox.Size = New-Object Drawing.Size(472, 86)
$form.Controls.Add($statusBox)

$dbStatus = New-Object Windows.Forms.Label
$dbStatus.Font = New-Object Drawing.Font("Segoe UI", 9)
$dbStatus.Location = New-Object Drawing.Point(16, 24)
$dbStatus.Size = New-Object Drawing.Size(210, 22)
$statusBox.Controls.Add($dbStatus)

$serverStatus = New-Object Windows.Forms.Label
$serverStatus.Font = New-Object Drawing.Font("Segoe UI", 9)
$serverStatus.Location = New-Object Drawing.Point(240, 24)
$serverStatus.Size = New-Object Drawing.Size(210, 22)
$statusBox.Controls.Add($serverStatus)

$worldStatus = New-Object Windows.Forms.Label
$worldStatus.Font = New-Object Drawing.Font("Segoe UI", 9)
$worldStatus.Location = New-Object Drawing.Point(16, 52)
$worldStatus.Size = New-Object Drawing.Size(434, 22)
$statusBox.Controls.Add($worldStatus)

function New-Button {
    param([string]$Text, [int]$X, [int]$Y, [int]$W, [int]$H)
    $button = New-Object Windows.Forms.Button
    $button.Text = $Text
    $button.Font = New-Object Drawing.Font("Segoe UI", 10, [Drawing.FontStyle]::Bold)
    $button.Location = New-Object Drawing.Point($X, $Y)
    $button.Size = New-Object Drawing.Size($W, $H)
    $button.FlatStyle = "System"
    $form.Controls.Add($button)
    return $button
}

$singleBtn = New-Button "Single Mode" 24 194 150 44
$hostBtn = New-Button "Host LAN" 185 194 150 44
$stopBtn = New-Button "Stop" 346 194 150 44
$backupBtn = New-Button "Backup World" 24 250 150 38
$resetBtn = New-Button "Reset World" 185 250 150 38
$folderBtn = New-Button "Open Folder" 346 250 150 38

$note = New-Object Windows.Forms.Label
$note.Text = "Single Mode and Host LAN use the same DefaultWorld save."
$note.Font = New-Object Drawing.Font("Segoe UI", 9)
$note.ForeColor = [Drawing.Color]::FromArgb(90, 96, 110)
$note.Location = New-Object Drawing.Point(28, 306)
$note.Size = New-Object Drawing.Size(468, 22)
$form.Controls.Add($note)

$singleBtn.Add_Click({ Start-Batch "Start-SingleMode.bat" })
$hostBtn.Add_Click({ Start-Batch "Start-HostLAN.bat" })
$stopBtn.Add_Click({ Start-Batch "Stop-Offline.bat" })
$backupBtn.Add_Click({ Start-Batch "Backup-DefaultWorld.bat" })
$resetBtn.Add_Click({ Start-Batch "Reset-DefaultWorld.bat" })
$folderBtn.Add_Click({ Open-Folder $Root })

function Refresh-Status {
    $dbUp = Test-Port 3307
    $serverUp = Test-Port 14445
    $dbStatus.Text = if ($dbUp) { "Database: Running" } else { "Database: Stopped" }
    $serverStatus.Text = if ($serverUp) { "Server: Running" } else { "Server: Stopped" }
    $worldStatus.Text = Get-WorldStatus
    $dbStatus.ForeColor = if ($dbUp) { [Drawing.Color]::ForestGreen } else { [Drawing.Color]::Firebrick }
    $serverStatus.ForeColor = if ($serverUp) { [Drawing.Color]::ForestGreen } else { [Drawing.Color]::Firebrick }
}

$timer = New-Object Windows.Forms.Timer
$timer.Interval = 2000
$timer.Add_Tick({ Refresh-Status })
$form.Add_Shown({
    Refresh-Status
    $timer.Start()
})
$form.Add_FormClosed({ $timer.Stop() })

[void]$form.ShowDialog()
