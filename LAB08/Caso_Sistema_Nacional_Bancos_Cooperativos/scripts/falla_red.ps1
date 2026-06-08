param(
    [ValidateSet('disconnect','connect')]
    [string]$Action = 'disconnect',
    [string]$Service = 'postgres_cusco'
)

function Get-ComposeNetwork {
    $projectName = (Get-Location).ProviderPath.Split('\\') | Select-Object -Last 1
    $candidate = $projectName.ToLower() -replace '[^a-z0-9]', '_'
    $networks = docker network ls --format '{{.Name}}' | Where-Object { $_ -like "${candidate}*" }
    if ($networks.Count -eq 1) {
        return $networks[0]
    }
    if ($networks.Count -gt 1) {
        Write-Host "Se encontraron varias redes posibles:" -ForegroundColor Yellow
        $networks | ForEach-Object { Write-Host "  $_" }
        throw "No se puede determinar la red de Docker Compose de forma única."
    }
    throw "No se encontró ninguna red de Docker Compose asociada al proyecto '$projectName'."
}

try {
    $network = Get-ComposeNetwork
    Write-Host "Red detectada: $network"

    if ($Action -eq 'disconnect') {
        Write-Host "Desconectando servicio $Service de la red $network..."
        docker network disconnect $network $Service
        Write-Host "Servicio $Service desconectado de la red." -ForegroundColor Green
        Write-Host "Para recuperar, ejecute: .\falla_red.ps1 -Action connect -Service $Service"
    }
    else {
        Write-Host "Reconectando servicio $Service a la red $network..."
        docker network connect $network $Service
        Write-Host "Servicio $Service reconectado a la red." -ForegroundColor Green
    }
}
catch {
    Write-Host "ERROR: $_" -ForegroundColor Red
    exit 1
}
