import dotenv from 'dotenv';
dotenv.config();

export const config = {
  // Identidad del nodo actual
  city: process.env.CITY,
  port: parseInt(process.env.PORT || '3000'),
  
  // Base de datos
  dbHost: process.env.DB_HOST || 'localhost',
  dbPort: parseInt(process.env.DB_PORT || '5432'),
  dbUser: process.env.DB_USER || 'admin',
  dbPassword: process.env.DB_PASSWORD || 'admin123',
  dbName: process.env.DB_NAME || 'inventario_arequipa',
  
  // Peers (otros nodos)
  peers: {
    arequipa: process.env.PEER_AREQUIPA || 'http://backend-arequipa:3000',
    lima: process.env.PEER_LIMA || 'http://backend-lima:3000',
    cusco: process.env.PEER_CUSCO || 'http://backend-cusco:3000',
  },
  
  // Timeouts (en ms)
  prepareTimeout: parseInt(process.env.PREPARE_TIMEOUT || '5000'),
  commitTimeout: parseInt(process.env.COMMIT_TIMEOUT || '5000'),
};

console.log(`Nodo inicializado: ${config.city}`);
console.log(`Base de datos: ${config.dbName}`);
console.log(`Peers: ${JSON.stringify(config.peers, null, 2)}`);
