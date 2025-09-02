rem @echo off
cd /d "%~dp0.."
echo Dossier courant après changement : %CD%


java -jar runner-dop-0.0.1-SNAPSHOT.jar etc\controller.json
pause
