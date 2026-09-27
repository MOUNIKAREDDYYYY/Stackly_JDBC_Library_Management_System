@echo off
IF NOT EXIST bin\com\library\LibraryApplication.class (
    echo Compiling first...
    call compile.bat
)

echo Starting Library Management System...
java -cp "bin;C:\Users\reddy\.m2\repository\com\mysql\mysql-connector-j\9.4.0\mysql-connector-j-9.4.0.jar" com.library.LibraryApplication
