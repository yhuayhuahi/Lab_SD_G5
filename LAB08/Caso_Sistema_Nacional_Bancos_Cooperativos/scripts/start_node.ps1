param(
    [string]$service = "postgres_cusco"
)

Write-Host "Starting container: $service"
docker-compose start $service
