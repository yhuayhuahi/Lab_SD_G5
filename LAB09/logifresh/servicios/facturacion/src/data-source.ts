import "reflect-metadata";
import { DataSource } from "typeorm";
import { Factura } from "./entities/Factura";
import { ItemFactura } from "./entities/ItemFactura";

export const AppDataSource = new DataSource({
  type: "postgres",
  host: process.env.DB_HOST || "localhost",
  port: parseInt(process.env.DB_PORT || "5432"),
  username: process.env.DB_USER || "admin",
  password: process.env.DB_PASSWORD || "secret",
  database: process.env.DB_NAME || "logifresh_facturacion",
  synchronize: false,
  logging: true,
  entities: [Factura, ItemFactura],  // ← DESCOMENTADO
  migrations: [],
  subscribers: [],
});
