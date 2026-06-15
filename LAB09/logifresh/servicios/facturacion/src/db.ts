import pg from "pg";

const { Pool } = pg;

export const pool = new Pool({
  host: process.env.DB_HOST || "localhost",
  port: parseInt(process.env.DB_PORT || "5432"),
  user: process.env.DB_USER || "admin",
  password: process.env.DB_PASSWORD || "secret",
  database: process.env.DB_NAME || "logifresh_facturacion",
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
});

// Probar conexión
export async function testConnection() {
  try {
    const client = await pool.connect();
    console.log("Conexión a PostgreSQL establecida");
    client.release();
    return true;
  } catch (error) {
    console.error("Error de conexión a PostgreSQL:", error);
    return false;
  }
}
