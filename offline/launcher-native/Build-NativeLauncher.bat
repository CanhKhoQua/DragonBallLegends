@echo off
setlocal EnableExtensions
set "ROOT=%~dp0..\.."
dotnet publish "%~dp0DragonBallLegends.Launcher\DragonBallLegends.Launcher.csproj" -c Release -r win-x64 --self-contained true -p:PublishSingleFile=true -p:IncludeNativeLibrariesForSelfExtract=true -o "%ROOT%\offline\launcher-native\publish"
