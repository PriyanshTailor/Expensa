# Expense Tracker Backend Startup Script (PowerShell)
# This script starts the Spring Boot backend server

Write-Host ""
Write-Host "====================================" -ForegroundColor Green
Write-Host " Smart Expense Tracker - Backend" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green
Write-Host ""

$mavenHome = "$env:TEMP\maven_install\apache-maven-3.9.6"
$backendDir = "$PSScriptRoot\backend_java"

Write-Host "Checking Maven installation..." -ForegroundColor Cyan
if (-not (Test-Path $mavenHome)) {
    Write-Host "ERROR: Maven not found at $mavenHome" -ForegroundColor Red
    Write-Host "Please run the setup first." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Maven found at: $mavenHome" -ForegroundColor Green
Write-Host ""

Push-Location $backendDir

Write-Host "Building backend (this may take a few minutes on first run)..." -ForegroundColor Yellow

& "$mavenHome\bin\mvn.cmd" clean install -DskipTests -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "====================================" -ForegroundColor Green
Write-Host " Starting Backend Server..." -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green
Write-Host ""
Write-Host "Server will run on: http://localhost:8080" -ForegroundColor Cyan
Write-Host "API Base URL: http://localhost:8080/api" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host ""

& "$mavenHome\bin\mvn.cmd" spring-boot:run

Pop-Location

