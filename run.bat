@echo off
setlocal

set "DIST_JAR=dist\DragonBallLegends.jar"
set "ROOT_JAR=Server.jar"

if exist "%DIST_JAR%" (
    java -server -Dfile.encoding=UTF-8 -jar "%DIST_JAR%"
) else (
    java -server -Dfile.encoding=UTF-8 -jar "%ROOT_JAR%"
)

pause
