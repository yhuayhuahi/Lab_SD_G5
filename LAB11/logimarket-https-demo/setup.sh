#!/bin/bash

echo "*** Configurando LogiMarket HTTPS Demo ***"

### Es de vital importancia tener instalado el openssl.
# En linux es asi: sudo <proveedor de apquetes por defecto> install openssl
# En mi caso Fedora: sudo dnf install openssl

# Generar certificado autofirmado
echo "Generando certificado autofirmado..."
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout nginx/ssl/server.key \
    -out nginx/ssl/server.crt \
    -subj "/C=PE/ST=Lima/L=Lima/O=LogiMarket/CN=logimarket.local" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "SUCCESS: Certificado generado exitosamente"
else
    echo "FAILURE: Error al generar el certificado"
    exit 1
fi
echo ""

# Verificar certificado
echo "Verificando certificado..."
ls -la nginx/ssl/
echo ""

# Verificar contenido del certificado
echo "Detalles del certificado:"
openssl x509 -in nginx/ssl/server.crt -text -noout | grep -E "Subject:|Not Before:|Not After:"
echo ""

# Construir y levantar contenedores
echo "Construyendo y levantando contenedores..."
docker compose up --build -d

if [ $? -eq 0 ]; then
    echo "SUCCESS: Contenedores levantados exitosamente"
else
    echo "FAILURE: Error al levantar contenedores"
    docker compose logs
    exit 1
fi
echo ""

# Esperar a que los servicios estén listos
echo "Esperando a que los servicios estén listos..."
sleep 10

# Verificar salud de la API 
echo "🔍 Verificando salud de la API"
curl -s http://localhost:3000/health
echo ""
echo ""

# Probar HTTPS con certificado autofirmado
echo "Probando conexión HTTPS..."
echo ""
echo "Inventario vía HTTPS:"
curl -k -s https://localhost/api/inventory | jq '.'
echo ""

# Verificar headers de seguridad
echo "Headers de seguridad:"
curl -k -s -I https://localhost/api/inventory | grep -i "strict\|x-frame\|x-content\|x-xss\|referrer\|permissions"
echo ""

echo "Conexión TLS:"
echo "Q" | openssl s_client -connect localhost:443 -servername logimarket.local 2>/dev/null | grep -E "Protocol|Cipher"
echo ""

echo "*** ¡Demostración lista! ***"
echo ""
echo "> Accesos:"
echo "  🔓 HTTP: http://localhost:3000/api/inventory"
echo "  🔒 HTTPS:  https://localhost/api/inventory"
echo ""
# echo "> Captura las siguientes pantallas:"
# echo "  1. HTTP: http://localhost:3000/api/inventory"
# echo "  2. Advertencia HTTPS: https://localhost/api/inventory"
# echo "  3. Conexión HTTPS exitosa (con el candado)"
# echo "  4. Detalles del certificado en el navegador"
# echo "  5. Headers de seguridad (DevTools → Network → Headers)"
# echo ""
echo "> Comandos útiles:"
echo "  Ver logs: docker-compose logs -f"
echo "  Detener: docker-compose down"
echo "  Ver certificado: openssl x509 -in nginx/ssl/server.crt -text -noout"
echo "====================================="
