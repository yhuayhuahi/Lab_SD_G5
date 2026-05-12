import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

// Inventario de la farmacia. Único objeto remoto del sistema.

public class Stock extends UnicastRemoteObject implements StockInterface {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(Stock.class.getName());

    // la clave es un nombre en minúsculas para búsqueda uniforme //
    private final Map<String, Medicine> medicines = new HashMap<>();

    public Stock() throws RemoteException {
        super();
    }

    // ── StockInterface ────────────────────────────────────────────────────────

    @Override
    public synchronized void addMedicine(String name, float price, int stock)
            throws StockException.DuplicateMedicineException,
                   StockException.InvalidAmountException,
                   RemoteException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser vacío.");
        }
        String key = name.trim().toLowerCase();
        if (medicines.containsKey(key)) {
            throw new StockException.DuplicateMedicineException(name.trim());
        }
        medicines.put(key, new Medicine(name.trim(), price, stock));
        logger.info("Medicina agregada: " + name.trim());
    }

    @Override
    public synchronized Medicine.Purchase buyMedicine(String name, int amount)
            throws StockException.MedicineNotFoundException,
                   StockException,
                   StockException.InvalidAmountException,
                   RemoteException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser vacío.");
        }
        if (amount <= 0) {
            throw new StockException.InvalidAmountException(
                "La cantidad debe ser mayor a cero. Recibido: " + amount);
        }
        Medicine med = medicines.get(name.trim().toLowerCase());
        if (med == null) {
            throw new StockException.MedicineNotFoundException(name.trim());
        }
        // decreaseStock lanza StockException si el stock es insuficiente
        med.decreaseStock(amount);

        Medicine.Purchase purchase = new Medicine.Purchase(
            med.getName(), med.getUnitPrice(), amount, med.getStock()
        );
        logger.info(String.format("Compra: %d x %s | Total: S/ %.2f",
            amount, med.getName(), purchase.getTotalPrice()));
        return purchase;
    }

    @Override
    public synchronized List<Medicine> getStockProducts() throws RemoteException {
        List<Medicine> snapshot = new ArrayList<>();
        for (Medicine m : medicines.values()) {
            try {
                snapshot.add(new Medicine(m.getName(), m.getUnitPrice(), m.getStock()));
            } catch (StockException.InvalidAmountException e) {
                logger.warning("Error inesperado copiando medicina: " + e.getMessage());
            }
        }
        snapshot.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return Collections.unmodifiableList(snapshot);
    }
}