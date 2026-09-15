@echo off
cd /d "%~dp0"
echo ========================================
echo   Starting World of Wonder (MVC Game)
echo ========================================

if not exist bin mkdir bin

echo Compiling Java source files...
dir /s /b src\*.java > sources.txt
javac -d bin @sources.txt
del sources.txt

echo Launching World of Wonder GUI...
java -cp bin com.worldofwonder.Main
pause

