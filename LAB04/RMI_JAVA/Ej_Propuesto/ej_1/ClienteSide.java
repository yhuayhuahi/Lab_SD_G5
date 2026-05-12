
import java.rmi.Naming;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

// Cliente principal del sistema RMI
public class ClienteSide {

    // Dirección donde está registrado el servidor
    private static final String PHARMACY_URL = "//localhost:1099/PHARMACY";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            // Obtiene la referencia remota del servidor
            StockInterface pharm = (StockInterface) Naming.lookup(PHARMACY_URL);

            System.out.println("╔══════════════════════════════════╗");
            System.out.println("║     Bienvenido a PharmaciaRMI    ║");
            System.out.println("╚══════════════════════════════════╝\n");

            boolean running = true;

            // Menú principal
            while (running) {
                printMenu();

                int selection = readInt(sc);
                sc.nextLine();

                switch (selection) {

                    // Lista todas las medicinas disponibles
                    case 1 -> listarProductos(pharm);

                    // Realiza una compra
                    case 2 -> comprarProducto(pharm, sc);

                    // Finaliza el programa
                    case 3 -> {
                        System.out.println("Hasta luego.");
                        running = false;
                    }

                    default -> System.out.println("[!] Opción inválida. Elija 1, 2 o 3.\n");
                }
            }

        } catch (Exception e) {

            // Error general de conexión con el servidor
            System.err.println("[Error de conexión] No se pudo contactar al servidor.");
            System.err.println("Detalle: " + e.getMessage());

        } finally {

            // Libera el Scanner
            sc.close();
        }
    }

    private static void printMenu() {
        System.out.println("┌─────────────────────────────────┐");
        System.out.println("│  1. Listar productos             │");
        System.out.println("│  2. Comprar producto             │");
        System.out.println("│  3. Salir                        │");
        System.out.println("└─────────────────────────────────┘");
        System.out.print("  Opción > ");
    }

    private static void listarProductos(StockInterface pharm) {

        try {
            // Obtiene una copia del inventario
            List<Medicine> productos = pharm.getStockProducts();

            if (productos.isEmpty()) {
                System.out.println("  (No hay productos disponibles.)\n");
                return;
            }

            System.out.println("\n  INVENTARIO");
            System.out.println("  ─────────────────────────────────────────────────");

            for (Medicine m : productos) {

                // Usa Medicine.toString()
                System.out.println("  " + m);
            }

            System.out.println("  ─────────────────────────────────────────────────\n");

        } catch (Exception e) {

            System.err.println("[Error] No se pudo obtener el inventario: " + e.getMessage());
        }
    }

    private static void comprarProducto(StockInterface pharm, Scanner sc) {

        System.out.print("  Nombre de la medicina : ");
        String name = sc.nextLine().trim();

        // Evita nombres vacíos
        if (name.isEmpty()) {
            System.out.println("[!] El nombre no puede estar vacío.\n");
            return;
        }

        System.out.print("  Cantidad              : ");
        int amount = readInt(sc);
        sc.nextLine();

        try {

            // Realiza la compra y devuelve el comprobante
            Medicine.Purchase purchase = pharm.buyMedicine(name, amount);

            System.out.println("\n" + purchase + "\n");

        } catch (StockException.MedicineNotFoundException e) {

            // La medicina no existe
            System.out.println("[!] " + e.getMessage() + "\n");

        } catch (StockException e) {

            // No hay suficiente stock
            System.out.println("[!] Stock insuficiente: " + e.getMessage() + "\n");

        } catch (StockException.InvalidAmountException e) {

            // Cantidad inválida
            System.out.println("[!] Cantidad inválida: " + e.getMessage() + "\n");

        } catch (Exception e) {

            // Error remoto o de red
            System.err.println("[Error de red] " + e.getMessage() + "\n");
        }
    }

    private static int readInt(Scanner sc) {

        try {

            // Lee un entero del teclado
            return sc.nextInt();

        } catch (InputMismatchException e) {

            // Si no es entero, retorna -1
            sc.nextLine();
            return -1;
        }
    }
}