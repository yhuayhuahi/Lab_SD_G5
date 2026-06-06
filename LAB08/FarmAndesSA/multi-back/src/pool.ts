import { Pool } from 'pg';
import dotenv from 'dotenv';

// Cargar variables de entorno
dotenv.config();

// Configuración del pool de conexiones
const pool = new Pool({
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT || '5432'),
  user: process.env.DB_USER || 'admin',
  password: process.env.DB_PASSWORD || 'admin123',
  database: process.env.DB_NAME || 'test_db',
  max: 20,              // Máximo de conexiones en el pool
  idleTimeoutMillis: 30000,  // Tiempo que una conexión inactiva se mantiene
  connectionTimeoutMillis: 2000, // Tiempo de espera para conectar
});

// Probar la conexión
pool.connect((err, client, release) => {
  if (err) {
    console.error('❌ Error conectando a PostgreSQL:', err.stack);
  } else {
    console.log('✅ Conectado a PostgreSQL exitosamente');
  }
  release();
});

export default pool;
