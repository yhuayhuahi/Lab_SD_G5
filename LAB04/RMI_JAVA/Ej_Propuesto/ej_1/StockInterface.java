import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

 // Interfaz RMI del inventario de la farmacia.
 // Punto de acceso remoto del sistema.
 
public interface StockInterface extends Remote {

    /**
     * Devuelve la lista de medicinas disponibles, ordenada alfabéticamente
     * Es una copia que al modificarla no afecta el inventario del servidor
     */
    List<Medicine> getStockProducts()
            throws RemoteException;

    void addMedicine(String name, float price, int stock)
            throws StockException.DuplicateMedicineException,
                   StockException.InvalidAmountException,
                   RemoteException;


    Medicine.Purchase buyMedicine(String name, int amount)
            throws StockException.MedicineNotFoundException,
                   StockException,
                   StockException.InvalidAmountException,
                   RemoteException;
}