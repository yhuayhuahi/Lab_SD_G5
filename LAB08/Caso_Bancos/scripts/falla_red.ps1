param (
    [Parameter(Mandatory=$true)]
    [ValidateSet("disconnect", "connect")]
    [string]$Action,

    [Parameter(Mandatory=$true)]
    [string]$Service
)

$Network = "bancos_net"

# Encontrar el nombre real de la red de docker compose (usualmente carpeta_bancos_net)
$RealNetwork = docker network ls --format '{{.Name}}' | Select-String -Pattern $Network

if (-not $RealNetwork) {
    Write-Host "No se encontró la red $Network" -ForegroundColor Red
    exit 1
}

if ($Action -eq "disconnect") {
    Write-Host "Desconectando $Service de la red $RealNetwork..." -ForegroundColor Yellow
    docker network disconnect $RealNetwork $Service
    Write-Host "Servicio $Service desconectado de la red." -ForegroundColor Green
    Write-Host "Para recuperar, ejecute: .\falla_red.ps1 -Action connect -Service $Service"
} else {
    Write-Host "Reconectando $Service a la red $RealNetwork..." -ForegroundColor Yellow
    docker network connect $RealNetwork $Service
    Write-Host "Servicio $Service reconectado a la red." -ForegroundColor Green
}