import { pool } from "../db";
import { PromocionService } from "./promocionService";

const promocionService = new PromocionService();

interface ItemInput {
  productoId: string;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
}

interface GenerarFacturaDTO {
  pedidoId: string;
  clienteId: string;
  clienteNombre?: string;
  clienteRuc?: string;
  clienteDireccion?: string;
  items: ItemInput[];
  promocionId?: string | null;
}

export class FacturaService {
  async calcular(items: ItemInput[], promocionId: string | null) {
    return promocionService.calcular(items, promocionId);
  }

  async generarFactura(data: GenerarFacturaDTO) {
    const client = await pool.connect();

    try {
      await client.query("BEGIN");

      // 1. Verificar si ya existe factura para este pedido
      const facturaExistente = await client.query(
        "SELECT factura_id FROM facturas WHERE pedido_id = $1",
        [data.pedidoId]
      );

      if (facturaExistente.rows.length > 0) {
        throw new Error(`DUPLICATE_INVOICE: Ya existe factura para pedido ${data.pedidoId}`);
      }

      // 2. Calcular totales con promociones
      const calculo = await this.calcular(data.items, data.promocionId || null);
      const igv = calculo.total * 0.18;
      const totalConIgv = calculo.total + igv;

      // 3. Insertar factura
      const facturaId = `FAC-${Date.now()}`;
      const facturaResult = await client.query(
        `INSERT INTO facturas 
         (factura_id, pedido_id, cliente_id, cliente_nombre, cliente_ruc, cliente_direccion, 
          subtotal, descuento, total, igv, estado, promocion_aplicada)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
         RETURNING id, factura_id, pedido_id, cliente_id, cliente_nombre, cliente_ruc, 
                   cliente_direccion, subtotal, descuento, total, igv, fecha_emision, 
                   estado, promocion_aplicada`,
        [
          facturaId,
          data.pedidoId,
          data.clienteId,
          data.clienteNombre || null,
          data.clienteRuc || null,
          data.clienteDireccion || null,
          calculo.subtotal,
          calculo.descuento,
          calculo.total,
          igv,
          "EMITIDA",
          calculo.promocionAplicada,
        ]
      );

      const factura = facturaResult.rows[0];

      // 4. Insertar items de factura
      for (const item of data.items) {
        const itemConDescuento = calculo.itemsConDescuento.find(
          (i) => i.productoId === item.productoId
        );

        await client.query(
          `INSERT INTO items_factura 
           (factura_id, producto_id, nombre_producto, cantidad, precio_unitario, descuento_aplicado)
           VALUES ($1, $2, $3, $4, $5, $6)`,
          [
            factura.id,
            item.productoId,
            item.nombre,
            item.cantidad,
            item.precioUnitario,
            itemConDescuento?.descuentoAplicado || 0,
          ]
        );
      }

      await client.query("COMMIT");

      return {
        facturaId: factura.factura_id,
        pedidoId: factura.pedido_id,
        clienteId: factura.cliente_id,
        clienteNombre: factura.cliente_nombre,
        clienteRuc: factura.cliente_ruc,
        clienteDireccion: factura.cliente_direccion,
        fechaEmision: factura.fecha_emision,
        subtotal: Number(factura.subtotal),
        descuentoTotal: Number(factura.descuento),
        total: Number(factura.total),
        igv: Number(factura.igv),
        totalConIgv: totalConIgv,
        promocionAplicada: factura.promocion_aplicada,
        estado: factura.estado,
      };
    } catch (error) {
      await client.query("ROLLBACK");
      throw error;
    } finally {
      client.release();
    }
  }

  // ✅ CORREGIDO: Obtener factura por ID (con items)
  async obtenerFacturaPorId(facturaId: string) {
    const client = await pool.connect();

    try {
      // Obtener factura por factura_id (string)
      const facturaResult = await client.query(
        `SELECT id, factura_id, pedido_id, cliente_id, cliente_nombre, cliente_ruc, cliente_direccion,
                subtotal, descuento, total, igv, fecha_emision, estado, promocion_aplicada
         FROM facturas WHERE factura_id = $1`,
        [facturaId]
      );

      if (facturaResult.rows.length === 0) {
        return null;
      }

      const factura = facturaResult.rows[0];

      // ✅ CORREGIDO: Usar factura.id (numérico) para buscar items
      const itemsResult = await client.query(
        `SELECT producto_id, nombre_producto, cantidad, precio_unitario, descuento_aplicado
         FROM items_factura WHERE factura_id = $1`,
        [factura.id]
      );

      const totalConIgv = Number(factura.total) + Number(factura.igv);

      return {
        facturaId: factura.factura_id,
        pedidoId: factura.pedido_id,
        clienteId: factura.cliente_id,
        clienteNombre: factura.cliente_nombre,
        clienteRuc: factura.cliente_ruc,
        clienteDireccion: factura.cliente_direccion,
        fechaEmision: factura.fecha_emision,
        items: itemsResult.rows.map((item: any) => ({
          productoId: item.producto_id,
          nombre: item.nombre_producto,
          cantidad: item.cantidad,
          precioUnitario: Number(item.precio_unitario),
          descuentoAplicado: Number(item.descuento_aplicado),
          subtotal: Number(item.precio_unitario) * item.cantidad - Number(item.descuento_aplicado),
        })),
        subtotal: Number(factura.subtotal),
        descuentoTotal: Number(factura.descuento),
        total: Number(factura.total),
        igv: Number(factura.igv),
        totalConIgv: totalConIgv,
        promocionAplicada: factura.promocion_aplicada,
        estado: factura.estado,
      };
    } finally {
      client.release();
    }
  }

  // ✅ CORREGIDO: Obtener factura por pedidoId (con items)
  async obtenerFacturaPorPedido(pedidoId: string) {
    const client = await pool.connect();

    try {
      // Obtener factura por pedido_id (string)
      const facturaResult = await client.query(
        `SELECT id, factura_id, pedido_id, cliente_id, cliente_nombre, cliente_ruc, cliente_direccion,
                subtotal, descuento, total, igv, fecha_emision, estado, promocion_aplicada
         FROM facturas WHERE pedido_id = $1`,
        [pedidoId]
      );

      if (facturaResult.rows.length === 0) {
        return null;
      }

      const factura = facturaResult.rows[0];

      // ✅ CORREGIDO: Usar factura.id (numérico) para buscar items
      const itemsResult = await client.query(
        `SELECT producto_id, nombre_producto, cantidad, precio_unitario, descuento_aplicado
         FROM items_factura WHERE factura_id = $1`,
        [factura.id]
      );

      const totalConIgv = Number(factura.total) + Number(factura.igv);

      return {
        facturaId: factura.factura_id,
        pedidoId: factura.pedido_id,
        clienteId: factura.cliente_id,
        clienteNombre: factura.cliente_nombre,
        clienteRuc: factura.cliente_ruc,
        clienteDireccion: factura.cliente_direccion,
        fechaEmision: factura.fecha_emision,
        items: itemsResult.rows.map((item: any) => ({
          productoId: item.producto_id,
          nombre: item.nombre_producto,
          cantidad: item.cantidad,
          precioUnitario: Number(item.precio_unitario),
          descuentoAplicado: Number(item.descuento_aplicado),
          subtotal: Number(item.precio_unitario) * item.cantidad - Number(item.descuento_aplicado),
        })),
        subtotal: Number(factura.subtotal),
        descuentoTotal: Number(factura.descuento),
        total: Number(factura.total),
        igv: Number(factura.igv),
        totalConIgv: totalConIgv,
        promocionAplicada: factura.promocion_aplicada,
        estado: factura.estado,
      };
    } finally {
      client.release();
    }
  }
}
