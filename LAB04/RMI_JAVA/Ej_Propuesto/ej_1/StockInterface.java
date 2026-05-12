
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

 // Interfaz RMI del inventario de la farmacia.
 // Punto de acceso remoto del sistema.
 
public interface StockInterface extends Remote {

    /**
     * Devuelve la lista de medicinas disponibles, ordenada alfabéticamente.
     * Es una copia defensiva: modificarla no afecta el inventario del servidor.
     */
    List<Medicine> getStockProducts()
            throws RemoteException;

    /**
     * Agrega una nueva medicina al inventario.
     *
     * @throws StockException.DuplicateMedicineException si ya existe
     * @throws StockException.InvalidAmountException     si precio o stock son negativos
     */
    void addMedicine(String name, float price, int stock)
            throws StockException.DuplicateMedicineException,
                   StockException.InvalidAmountException,
                   RemoteException;

    /**
     * Realiza la compra y devuelve el comprobante.
     *
     * @param name   nombre de la medicina (búsqueda case-insensitive)
     * @param amount cantidad deseada (debe ser > 0)
     * @return Medicine.Purchase con el detalle de la transacción
     * @throws StockException.MedicineNotFoundException si no existe
     * @throws StockException                           si stock insuficiente
     * @throws StockException.InvalidAmountException    si amount <= 0
     */
    Medicine.Purchase buyMedicine(String name, int amount)
            throws StockException.MedicineNotFoundException,
                   StockException,
                   StockException.InvalidAmountException,
                   RemoteException;
}