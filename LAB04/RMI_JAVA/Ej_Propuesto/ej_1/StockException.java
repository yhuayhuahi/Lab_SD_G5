package Medicinas;

/**
 * Excepción base para errores de stock (stock agotado o insuficiente).
 *
 * MEJORA: este archivo centraliza todas las excepciones de negocio
 * del sistema como clases internas estáticas, reemplazando el uso
 * de "throws Exception" genérico en toda la aplicación.
 *
 * Jerarquía:
 *
 *   Exception
 *   └── StockException                  (stock agotado / insuficiente)
 *   └── StockException.MedicineNotFoundException  (medicina no encontrada)
 *   └── StockException.InvalidAmountException     (cantidad o precio inválido)
 *   └── StockException.DuplicateMedicineException (medicina ya existe)
 */
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

    // ── DuplicateMedicineException ────────────────────────────────────────────

    /**
     * Lanzada al intentar agregar una medicina con un nombre ya registrado.
     */
    public static class DuplicateMedicineException extends Exception {

        private static final long serialVersionUID = 1L;

        public DuplicateMedicineException(String medicineName) {
            super("La medicina '" + medicineName + "' ya existe en el inventario.");
        }
    }
}