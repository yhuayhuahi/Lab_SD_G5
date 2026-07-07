$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

$jdk17 = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"

if (Test-Path $jdk17) {
    $env:JAVA_HOME = $jdk17
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}

Write-Host "Compilando conversor gRPC..." -ForegroundColor Cyan
mvn compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error al compilar el conversor gRPC." -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}

Write-Host "Iniciando interfaz grafica del conversor..." -ForegroundColor Green
mvn exec:java "-Dexec.mainClass=com.lab.grpc.converter.ConverterGUI"

Read-Host "Presiona Enter para salir"
