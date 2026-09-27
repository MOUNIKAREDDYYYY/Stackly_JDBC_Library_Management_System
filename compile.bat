@echo off
IF NOT EXIST bin mkdir bin
echo Finding all Java files...
dir /s /b src\*.java > sources.txt

echo Compiling Java source files...
javac -cp "C:\Users\reddy\.m2\repository\com\mysql\mysql-connector-j\9.4.0\mysql-connector-j-9.4.0.jar" -d bin @sources.txt

IF %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
) ELSE (
    echo Compilation failed.
)
