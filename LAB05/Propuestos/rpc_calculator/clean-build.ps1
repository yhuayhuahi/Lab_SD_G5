$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

$jdk17 = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"

if (Test-Path $jdk17) {
    $env:JAVA_HOME = $jdk17
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

Write-Host "Cerrando procesos Java..." -ForegroundColor Yellow
taskkill /F /IM java.exe /T 2>$null
taskkill /F /IM javaw.exe /T 2>$null

Write-Host "Limpiando y recompilando calculadora RPC..." -ForegroundColor Cyan
mvn clean compile

Read-Host "Presiona Enter para salir"
