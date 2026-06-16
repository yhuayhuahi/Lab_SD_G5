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

    // PROMO-2X1: 1 unidad del producto más barato es gratis
    // Aplica cuando el carrito tiene 2 o más unidades en total
    if (promocionId === "PROMO-2X1") {
      const totalUnidades = items.reduce((sum, i) => sum + i.cantidad, 0);
      if (totalUnidades >= 2) {
        const itemMasBarato = [...items].sort(
          (a, b) => a.precioUnitario - b.precioUnitario
        )[0];

        descuento = itemMasBarato.precioUnitario;

        const idx = itemsConDescuento.findIndex(
          (i) => i.productoId === itemMasBarato.productoId
        );
        if (idx !== -1) {
          itemsConDescuento[idx].descuentoAplicado = descuento;
          itemsConDescuento[idx].subtotalFinal -= descuento;
        }

        detalleDescuento = `Promoción 2x1: 1 unidad de ${itemMasBarato.productoId} gratis (S/${descuento.toFixed(2)})`;
        promocionAplicada = "PROMO-2X1";
      }
    }
    // PROMO-10OFF: 10% de descuento si el subtotal supera S/100
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
