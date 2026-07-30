#define AppName "DragonBallLegends"
#define AppVersion "1.0.0"
#define SourceDir "..\..\..\DragonBallLegends_OfflinePack"
#define OutputDir "..\..\..\installer-output"

[Setup]
AppId={{7BEB5312-675C-47BB-865D-3B4314120A3F}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher=DragonBallLegends
DefaultDirName={localappdata}\Programs\{#AppName}
DefaultGroupName={#AppName}
DisableProgramGroupPage=yes
OutputDir={#OutputDir}
OutputBaseFilename=DragonBallLegends_Setup
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=lowest
UninstallDisplayIcon={app}\DragonBallLegends Launcher.exe

[Files]
Source: "{#SourceDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs; Excludes: "saves\*,logs\*,server\log\*,runtime\dbdata\*,runtime\.db_initialized,runtime\offline-db.pid,runtime\offline-server.pid,runtime\offline-world.txt,client_build.log"

[Icons]
Name: "{group}\DragonBallLegends"; Filename: "{app}\DragonBallLegends Launcher.exe"; WorkingDir: "{app}"
Name: "{commondesktop}\DragonBallLegends"; Filename: "{app}\DragonBallLegends Launcher.exe"; WorkingDir: "{app}"; Tasks: desktopicon

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Shortcuts:"; Flags: unchecked

[Run]
Filename: "{app}\DragonBallLegends Launcher.exe"; Description: "Launch DragonBallLegends"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: filesandordirs; Name: "{app}\logs"
