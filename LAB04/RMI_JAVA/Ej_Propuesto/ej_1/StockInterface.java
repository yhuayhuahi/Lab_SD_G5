
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

 // Interfaz RMI del inventario de la farmacia.
 // Punto de acceso remoto del sistema.
 
public interface StockInterface extends Remote { // para ser un objeto remoto RMI

    //
     //Devuelve la lista de medicinas disponibles en orden alfab
     // Copia defensiva, no expone la colección del servidor
     //
    List<Medicine> getStockProducts()
            throws RemoteException; 

    //Agrega una nueva medicina al inventario.
     
     // DuplicateMedicineException si ya existe
     // InvalidAmountException     si precio o stock son negativos
     
    void addMedicine(String name, float price, int stock)
            throws StockException.DuplicateMedicineException,
                   StockException.InvalidAmountException,
                   RemoteException;


       // Medicine.Purchase lo que hace es comprar una cantidad de una medicina y devolver un comprobante de compra            
    Medicine.Purchase buyMedicine(String name, int amount)
            throws StockException.MedicineNotFoundException,
                   StockException,
                   StockException.InvalidAmountException,
                   RemoteException;
}