import "reflect-metadata";
import { createApp } from "./app";
import { AppDataSource } from "./data-source";

const PORT = parseInt(process.env.PORT || "8083");

async function main() {
  try {
    // Conectar a la base de datos
    await AppDataSource.initialize();
    console.log("Conexión a PostgreSQL establecida (n.n)");

    const app = createApp();

    app.listen(PORT, "0.0.0.0", () => {
      console.log(`Servicio de facturación corriendo en http://localhost:${PORT}`);
      console.log(`Health check: http://localhost:${PORT}/health`);
      console.log(`Endpoints disponibles:`);     
    });
  } catch (error) {
    console.error("Error al iniciar el servicio:", error);
    process.exit(1);
  }
}

main();

