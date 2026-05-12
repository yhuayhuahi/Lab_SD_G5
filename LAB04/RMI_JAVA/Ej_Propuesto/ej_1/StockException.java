//Centraliza todas las excepciones de negocio

public class StockException extends Exception {

    private static final long serialVersionUID = 1L;

    public StockException(String msg) { // 
        super(msg); // mensaje genérico 
    }

    public StockException(String msg, Throwable cause) {
        super(msg, cause); // encadena la causa original 
    }

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