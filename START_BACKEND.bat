@echo off
REM Expense Tracker Backend Startup Script
REM This script starts the Spring Boot backend server

echo.
echo ====================================
echo  Smart Expense Tracker - Backend
echo ====================================
echo.

set MAVEN_HOME=%TEMP%\maven_install\apache-maven-3.9.6
set BACKEND_DIR="%cd%\backend_java"

echo Checking Maven installation...
if not exist "%MAVEN_HOME%" (
    echo ERROR: Maven not found at %MAVEN_HOME%
    echo Please run the setup script first.
    pause
    exit /b 1
)

echo Maven found at: %MAVEN_HOME%
echo.

cd /d %BACKEND_DIR%

echo Building backend (this may take a few minutes on first run)...
call "%MAVEN_HOME%\bin\mvn.cmd" clean install -DskipTests -q

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo ====================================
echo  Starting Backend Server...
echo ====================================
echo.
echo Server will run on: http://localhost:8080
echo Press Ctrl+C to stop the server
echo.

call "%MAVEN_HOME%\bin\mvn.cmd" spring-boot:run

pause

