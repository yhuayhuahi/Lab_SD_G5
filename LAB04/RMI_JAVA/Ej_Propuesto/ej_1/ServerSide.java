
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Logger;


 // Punto de entrada del servidor RMI de la farmacia.

 //  Logger en lugar de System.out para que sea más fácil de configurar y mantener.
 
public class ServerSide {

    private static final int    RMI_PORT = 1099; // puerto estándar de RMI Registry
    private static final String BINDING  = "//localhost:" + RMI_PORT + "/PHARMACY"; // URL de registro
    private static final Logger logger   = Logger.getLogger(ServerSide.class.getName()); // para mensajes

    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(RMI_PORT);
                logger.info("RMI Registry creado en puerto " + RMI_PORT + ".");
            } catch (Exception e) {
                logger.info("RMI Registry ya activo en puerto " + RMI_PORT + "."); // si ya existe, seguimos adelante
            }

            Stock pharmacy = new Stock();
            pharmacy.addMedicine("Paracetamol", 3.20f, 10);
            pharmacy.addMedicine("Mejoral",     2.00f, 20);
            pharmacy.addMedicine("Amoxicilina", 1.00f, 30);
            pharmacy.addMedicine("Aspirina",    5.00f, 40);
            pharmacy.addMedicine("Ibuprofeno",  4.50f, 25);

            Naming.rebind(BINDING, pharmacy); // registra el objeto con el nombre definido 
            logger.info("Servidor listo. Farmacia registrada en '" + BINDING + "'.");

        } catch (StockException.DuplicateMedicineException e) {
            logger.severe("Inventario inicial con duplicado: " + e.getMessage());
        } catch (StockException.InvalidAmountException e) {
            logger.severe("Inventario inicial con monto inválido: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}