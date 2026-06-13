interface Item {
  productoId: string;
  cantidad: number;
  precioUnitario: number;
}

interface CalculoResultado {
  subtotal: number;
  descuento: number;
  total: number;
  detalleDescuento: string;
  promocionAplicada: string | null;
  itemsConDescuento: Array<{
    productoId: string;
    cantidad: number;
    precioUnitario: number;
    descuentoAplicado: number;
    subtotalFinal: number;
  }>;
}

export class PromocionService {
  calcular(items: Item[], promocionId: string | null): CalculoResultado {
    // Calcular subtotal
    const subtotal = items.reduce(
      (sum, item) => sum + item.precioUnitario * item.cantidad,
      0
    );

    let descuento = 0;
    let detalleDescuento = "No se aplicaron promociones";
    let promocionAplicada: string | null = null;
    let itemsConDescuento = items.map((item) => ({
      productoId: item.productoId,
      cantidad: item.cantidad,
      precioUnitario: item.precioUnitario,
      descuentoAplicado: 0,
      subtotalFinal: item.precioUnitario * item.cantidad,
    }));

    // Promoción 2x1: el producto más barato es gratis
    if (promocionId === "PROMO-2X1") {
      const itemsOrdenados = [...items].sort(
        (a, b) => a.precioUnitario - b.precioUnitario
      );
      const productoGratis = itemsOrdenados[0];

      if (productoGratis && productoGratis.cantidad >= 2) {
        const unidadesGratis = Math.floor(productoGratis.cantidad / 2);
        descuento = unidadesGratis * productoGratis.precioUnitario;

        const itemIndex = itemsConDescuento.findIndex(
          (i) => i.productoId === productoGratis.productoId
        );
        if (itemIndex !== -1) {
          itemsConDescuento[itemIndex].descuentoAplicado = descuento;
          itemsConDescuento[itemIndex].subtotalFinal =
            productoGratis.cantidad * productoGratis.precioUnitario - descuento;
        }

        detalleDescuento = `Promoción 2x1 aplicada en ${productoGratis.productoId}`;
        promocionAplicada = "PROMO-2X1";
      }
    }
    // Promoción 10% OFF: descuento si subtotal > 100
    else if (promocionId === "PROMO-10OFF" && subtotal > 100) {
      descuento = subtotal * 0.1;
      detalleDescuento = "10% de descuento por compra mayor a S/100";
      promocionAplicada = "PROMO-10OFF";
    }

    const total = subtotal - descuento;

    return {
      subtotal,
      descuento,
      total,
      detalleDescuento,
      promocionAplicada,
      itemsConDescuento,
    };
  }
}
