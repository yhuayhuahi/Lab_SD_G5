param(
    [string]$service = "postgres_cusco"
)

Write-Host "Stopping container: $service"
docker-compose stop $service
