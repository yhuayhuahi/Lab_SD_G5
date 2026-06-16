#!/bin/bash

# ============================================================
# RESET DE BASES DE DATOS - LOGIFRESH
# ============================================================

echo "🧹 Iniciando limpieza de bases de datos..."
echo "=================================================="

# ------------------------------------------------------------
# 1. MONGODB - Servicio de Pedidos
# ------------------------------------------------------------
echo ""
echo "📦 Limpiando MongoDB (logifresh-mongo)..."
docker exec logifresh-mongo mongosh --eval "
  db.getSiblingDB('pedidos').dropDatabase();
  print('✅ Base de datos pedidos eliminada');
" 2>&1

if [ $? -eq 0 ]; then
  echo "✅ MongoDB limpiado correctamente"
else
  echo "⚠️  Error al limpiar MongoDB"
fi

# ------------------------------------------------------------
# 2. POSTGRESQL - Servicio de Facturación
# ------------------------------------------------------------
echo ""
echo "🐘 Limpiando PostgreSQL (logifresh-postgres-facturacion)..."
docker exec logifresh-postgres-facturacion psql -U admin -d logifresh_facturacion -c "
  TRUNCATE TABLE items_factura, facturas RESTART IDENTITY CASCADE;
" 2>&1

if [ $? -eq 0 ]; then
  echo "✅ PostgreSQL limpiado correctamente"
else
  echo "⚠️  Error al limpiar PostgreSQL"
fi

# ------------------------------------------------------------
# 3. REDIS - Servicio de Inventario y Transporte
# ------------------------------------------------------------
echo ""
echo "⚡ Limpiando Redis (logifresh-redis-inventario)..."
docker exec logifresh-redis-inventario redis-cli FLUSHALL 2>&1

if [ $? -eq 0 ]; then
  echo "✅ Redis limpiado correctamente"
else
  echo "⚠️  Error al limpiar Redis"
fi

# ------------------------------------------------------------
# 4. CARGAR STOCK INICIAL EN REDIS
# ------------------------------------------------------------
echo ""
echo "🔄 Cargando stock inicial en Redis..."
docker exec logifresh-redis-inventario redis-cli MSET \
  "stock:PROD-YOGURT" "150" \
  "stock:PROD-LECHE" "45" \
  "stock:PROD-QUELLAVES" "0" \
  "stock:PROD-MANTECA" "23" \
  "stock:PROD-POLLO" "78" \
  "stock:PROD-CARNE" "45" \
  2>&1

if [ $? -eq 0 ]; then
  echo "✅ Stock inicial cargado correctamente"
else
  echo "⚠️  Error al cargar stock inicial"
fi

# ------------------------------------------------------------
# 5. VERIFICAR ESTADO FINAL
# ------------------------------------------------------------
echo ""
echo "=================================================="
echo "📊 Resumen de bases de datos:"

echo ""
echo "📦 MongoDB - Pedidos:"
docker exec logifresh-mongo mongosh --eval "db.getSiblingDB('pedidos').getCollectionNames();" 2>&1 | head -5

echo ""
echo "🐘 PostgreSQL - Facturación:"
docker exec logifresh-postgres-facturacion psql -U admin -d logifresh_facturacion -c "
  SELECT 'Facturas: ' || COUNT(*) FROM facturas
  UNION ALL
  SELECT 'Items: ' || COUNT(*) FROM items_factura;
" 2>&1

echo ""
echo "⚡ Redis - Stock:"
docker exec logifresh-redis-inventario redis-cli KEYS "stock:*" 2>&1 | head -10

echo ""
echo "=================================================="
echo "✅ Bases de datos listas para las pruebas"
echo "=================================================="
