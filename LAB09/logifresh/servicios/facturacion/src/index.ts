import { createApp } from "./app.js";
import { testConnection } from "./db.js";

const PORT = parseInt(process.env.PORT || "8083");

async function main() {
  // Probar conexión a la base de datos
  const connected = await testConnection();
  
  if (!connected) {
    console.error("No se pudo conectar a PostgreSQL. Saliendo...");
    process.exit(1);
  }

  const app = createApp();

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`🚀 Servicio de facturación corriendo en http://localhost:${PORT}`);
    console.log(`📋 Health check: http://localhost:${PORT}/health`);
    console.log(`🧾 Endpoints disponibles:`);
    console.log(`   POST   /api/facturas/calcular`);
    console.log(`   POST   /api/facturas/generar`);
    console.log(`   GET    /api/facturas/:id`);
    console.log(`   GET    /api/facturas/pedido/:pedidoId`);
  });
}

main()
