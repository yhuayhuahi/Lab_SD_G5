import express, { Application, Request, Response } from "express";
import cors from "cors";
import { PromocionService } from "./services/promocionService";

const promocionService = new PromocionService();

export function createApp(): Application {
  const app = express();

  app.use(cors());
  app.use(express.json());

  // Health check
  app.get("/health", (_: Request, res: Response) => {
    res.json({
      status: "ok",
      service: "facturacion",
      timestamp: new Date().toISOString(),
    });
  });

  // ========== ENDPOINTS DE FACTURACIÓN ==========

  // POST /api/facturas/calcular - Calcular totales (sin guardar)
  app.post("/api/facturas/calcular", (req: Request, res: Response) => {
    try {
      const { items, promocionId } = req.body;

      // Validaciones
      if (!items || !Array.isArray(items) || items.length === 0) {
        return res.status(400).json({
          error: "VALIDATION_ERROR",
          mensaje: "El campo items es requerido y debe ser un array no vacío",
          timestamp: new Date().toISOString(),
        });
      }

      // Validar que cada item tenga los campos requeridos
      for (const item of items) {
        if (!item.productoId || typeof item.cantidad !== "number" || typeof item.precioUnitario !== "number") {
          return res.status(400).json({
            error: "VALIDATION_ERROR",
            mensaje: "Cada item debe tener productoId, cantidad y precioUnitario",
            timestamp: new Date().toISOString(),
          });
        }
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

  // POST /api/facturas/generar - Generar y guardar factura (pendiente)
  app.post("/api/facturas/generar", (_req: Request, res: Response) => {
    res.status(501).json({ mensaje: "Por implementar" });
  });

  // GET /api/facturas/:id - Obtener factura por ID (pendiente)
  app.get("/api/facturas/:id", (_req: Request, res: Response) => {
    res.status(501).json({ mensaje: "Por implementar" });
  });

  // GET /api/facturas/pedido/:pedidoId - Obtener factura por pedido (pendiente)
  app.get("/api/facturas/pedido/:pedidoId", (_req: Request, res: Response) => {
    res.status(501).json({ mensaje: "Por implementar" });
  });

  return app;
}
