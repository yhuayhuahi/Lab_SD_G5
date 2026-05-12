package Medicinas;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Medicine implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final float  unitPrice;
    private int          stock;

    /**
     * @param name      nombre de la medicina (no nulo, no vacío)
     * @param unitPrice precio por unidad (>= 0)
     * @param stock     cantidad disponible (>= 0)
     */
    public Medicine(String name, float unitPrice, int stock)
            throws StockException.InvalidAmountException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser vacío.");
        }
        if (unitPrice < 0) {
            throw new StockException.InvalidAmountException(
                "El precio unitario no puede ser negativo: " + unitPrice);
        }
        if (stock < 0) {
            throw new StockException.InvalidAmountException(
                "El stock inicial no puede ser negativo: " + stock);
        }
        this.name      = name.trim();
        this.unitPrice = unitPrice;
        this.stock     = stock;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getName()      { return name;      }
    public float  getUnitPrice() { return unitPrice; }
    public int    getStock()     { return stock;     }

    // ── Lógica de dominio ─────────────────────────────────────────────────────

    public void decreaseStock(int amount)
            throws StockException, StockException.InvalidAmountException {
        if (amount <= 0) {
            throw new StockException.InvalidAmountException(
                "La cantidad debe ser mayor a cero. Recibido: " + amount);
        }
        if (stock == 0) {
            throw new StockException("Stock agotado para: " + name);
        }
        if (stock < amount) {
            throw new StockException(
                "Stock insuficiente para '" + name + "'. " +
                "Disponible: " + stock + ", solicitado: " + amount);
        }
        this.stock -= amount;
    }

    @Override
    public String toString() {
        return String.format("%-16s | Precio: S/ %6.2f | Stock: %3d ud.",
            name, unitPrice, stock);
    }

 //Serializable para que RMI pueda transferirlo al cliente por valor.
    
    public static class Purchase implements Serializable {

        private static final long serialVersionUID = 1L;
        private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        private final String        medicineName;
        private final float         unitPrice;
        private final int           quantity;
        private final float         totalPrice;
        private final int           stockAfter;
        private final LocalDateTime purchaseTime;

        public Purchase(String medicineName, float unitPrice,
                        int quantity, int stockAfter) {
            this.medicineName = medicineName;
            this.unitPrice    = unitPrice;
            this.quantity     = quantity;
            this.totalPrice   = unitPrice * quantity;
            this.stockAfter   = stockAfter;
            this.purchaseTime = LocalDateTime.now();
        }

        public String        getMedicineName() { return medicineName; }
        public float         getUnitPrice()    { return unitPrice;    }
        public int           getQuantity()     { return quantity;     }
        public float         getTotalPrice()   { return totalPrice;   }
        public int           getStockAfter()   { return stockAfter;   }
        public LocalDateTime getPurchaseTime() { return purchaseTime; }

        @Override
        public String toString() {
            return  "╔══════════════════════════════════╗\n"
                  + "║       COMPROBANTE DE COMPRA      ║\n"
                  + "╠══════════════════════════════════╣\n"
                  + String.format("║  Producto  : %-20s║%n", medicineName)
                  + String.format("║  Cantidad  : %-20d║%n", quantity)
                  + String.format("║  P. Unidad : S/ %-17.2f║%n", unitPrice)
                  + String.format("║  TOTAL     : S/ %-17.2f║%n", totalPrice)
                  + String.format("║  Stock rest: %-20d║%n", stockAfter)
                  + String.format("║  Fecha/hora: %-20s║%n", purchaseTime.format(FMT))
                  + "╚══════════════════════════════════╝";
        }
    }
}