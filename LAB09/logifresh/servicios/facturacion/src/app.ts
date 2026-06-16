import express, { Application, Request, Response } from "express";
import cors from "cors";
import { PromocionService } from "./services/promocionService.js";
import { FacturaService } from "./services/facturaService.js";

const promocionService = new PromocionService();
const facturaService = new FacturaService();

export function createApp(): Application {
  const app = express();

  app.use(cors());
  app.use(express.json());

  // Health check
  app.get("/health", (_req: Request, res: Response) => {
    res.json({
      status: "ok",
      service: "facturacion",
      timestamp: new Date().toISOString(),
    });
  });

  // POST /api/facturas/calcular
  app.post("/api/facturas/calcular", (req: Request, res: Response) => {
    try {
      const { items, promocionId } = req.body;

      if (!items || !Array.isArray(items) || items.length === 0) {
        return res.status(400).json({
          error: "VALIDATION_ERROR",
          mensaje: "El campo items es requerido y debe ser un array no vacío",
          timestamp: new Date().toISOString(),
        });
      }

      const resultado = promocionService.calcular(items, promocionId || null);

      res.json({
        subtotal: resultado.subtotal,
        descuento: resultado.descuento,
        total: resultado.total,
        detalleDescuento: resultado.detalleDescuento,
        promocionAplicada: resultado.promocionAplicada,
      });
    } catch (error) {
      console.error("Error en /calcular:", error);
      res.status(500).json({
        error: "INTERNAL_ERROR",
        mensaje: "Error al calcular la factura",
        timestamp: new Date().toISOString(),
      });
    }
  });

  // POST /api/facturas/generar
  app.post("/api/facturas/generar", async (req: Request, res: Response) => {
    try {
      const { pedidoId, clienteId, clienteNombre, clienteRuc, clienteDireccion, items } = req.body;
      // Acepta tanto promocionId (interno) como promocionAplicada (enviado por pedidos)
      const promocionId = req.body.promocionId ?? req.body.promocionAplicada ?? null;

      if (!pedidoId || !clienteId || !items || !Array.isArray(items) || items.length === 0) {
        return res.status(400).json({
          error: "VALIDATION_ERROR",
          mensaje: "Faltan campos requeridos: pedidoId, clienteId, items",
          timestamp: new Date().toISOString(),
        });
      }

      const factura = await facturaService.generarFactura({
        pedidoId,
        clienteId,
        clienteNombre,
        clienteRuc,
        clienteDireccion,
        items,
        promocionId,
      });

      res.status(201).json(factura);
    } catch (error: any) {
      console.error("Error en /generar:", error);

      if (error.message?.startsWith("DUPLICATE_INVOICE:")) {
        const facturaExistente = error.message.split("DUPLICATE_INVOICE:")[1]?.trim();
        return res.status(409).json({
          error: "DUPLICATE_INVOICE",
          mensaje: `Ya existe una factura para el pedido ${req.body.pedidoId}`,
          facturaExistente,
          timestamp: new Date().toISOString(),
        });
      }

      res.status(500).json({
        error: "INTERNAL_ERROR",
        mensaje: "Error al generar la factura",
        timestamp: new Date().toISOString(),
      });
    }
  });

  // GET /api/facturas/pedido/:pedidoId  — debe ir ANTES de /:id para que Express no capture "pedido" como id
  app.get("/api/facturas/pedido/:pedidoId", async (req: Request, res: Response) => {
    try {
      const { pedidoId } = req.params;

      if (!pedidoId || typeof pedidoId !== "string") {
        return res.status(400).json({
          error: "VALIDATION_ERROR",
          mensaje: "ID de pedido inválido",
          timestamp: new Date().toISOString(),
        });
      }

      const factura = await facturaService.obtenerFacturaPorPedido(pedidoId);

      if (!factura) {
        return res.status(404).json({
          error: "NOT_FOUND",
          mensaje: `No existe factura para el pedido: ${pedidoId}`,
          timestamp: new Date().toISOString(),
        });
      }

      res.json(factura);
    } catch (error) {
      console.error("Error en /pedido/:pedidoId:", error);
      res.status(500).json({
        error: "INTERNAL_ERROR",
        mensaje: "Error al obtener la factura",
        timestamp: new Date().toISOString(),
      });
    }
  });

  // GET /api/facturas/:id
  app.get("/api/facturas/:id", async (req: Request, res: Response) => {
    try {
      const { id } = req.params;
      
      // Asegurar que id es string y no undefined
      if (!id || typeof id !== "string") {
        return res.status(400).json({
          error: "VALIDATION_ERROR",
          mensaje: "ID de factura inválido",
          timestamp: new Date().toISOString(),
        });
      }

      const factura = await facturaService.obtenerFacturaPorId(id);

      if (!factura) {
        return res.status(404).json({
          error: "NOT_FOUND",
          mensaje: `No existe factura con ID: ${id}`,
          timestamp: new Date().toISOString(),
        });
      }

      res.json(factura);
    } catch (error) {
      console.error("Error en /facturas/:id:", error);
      res.status(500).json({
        error: "INTERNAL_ERROR",
        mensaje: "Error al obtener la factura",
        timestamp: new Date().toISOString(),
      });
    }
  });

  return app;
}
