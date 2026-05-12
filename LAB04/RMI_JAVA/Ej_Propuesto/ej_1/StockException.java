//Centraliza todas las excepciones

public class StockException extends Exception {

    private static final long serialVersionUID = 1L;

    public StockException(String msg) {
        super(msg);
    }

    public StockException(String msg, Throwable cause) {
        super(msg, cause);
    }

    // ── MedicineNotFoundException ─────────────────────────────────────────────

    /**
     * Lanzada cuando se busca una medicina que no existe en el inventario.
     * Permite al cliente mostrar un mensaje diferente al de stock insuficiente.
     */
    public static class MedicineNotFoundException extends Exception {

        private static final long serialVersionUID = 1L;
        private final String medicineName;

        public MedicineNotFoundException(String medicineName) {
            super("Medicina no encontrada: '" + medicineName + "'. " +
                  "Use la opción 1 para ver los productos disponibles.");
            this.medicineName = medicineName;
        }

        public String getMedicineName() { return medicineName; }
    }

    // ── InvalidAmountException ────────────────────────────────────────────────

    /**
     * Lanzada cuando una cantidad o precio es <= 0 o viola un invariante numérico.
     * Es un error del usuario (input incorrecto), no del sistema.
     */
    public static class InvalidAmountException extends Exception {

        private static final long serialVersionUID = 1L;

        public InvalidAmountException(String msg) {
            super(msg);
        }

        public InvalidAmountException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }


    public static class DuplicateMedicineException extends Exception {

        private static final long serialVersionUID = 1L;

        public DuplicateMedicineException(String medicineName) {
            super("La medicina '" + medicineName + "' ya existe en el inventario.");
        }
    }
}