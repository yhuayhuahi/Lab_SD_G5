const { createClient } = require('redis');

const client = createClient({
  socket: {
    host: process.env.REDIS_HOST || 'localhost',
    port: parseInt(process.env.REDIS_PORT) || 6379,
  },
});

client.on('error', (err) => {
  console.error('[Redis] Error de conexión:', err.message);
});

client.on('connect', () => {
  console.log('[Redis] Conectado exitosamente');
});

const connectRedis = async () => {
  if (!client.isOpen) {
    await client.connect();
  }
};

module.exports = { client, connectRedis };