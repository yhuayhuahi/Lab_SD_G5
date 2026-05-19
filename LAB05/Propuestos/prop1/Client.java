import java.rmi.Naming;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {

        try {

            // Conectarse al servidor
            Calculator calculator = (Calculator)
                    Naming.lookup("rmi://localhost/CalculatorService");

            Scanner sc = new Scanner(System.in);

            System.out.print("Ingrese primer número: ");
            double a = sc.nextDouble();

            System.out.print("Ingrese segundo número: ");
            double b = sc.nextDouble();

            // Invocaciones remotas
            double multiplication = calculator.multiply(a, b);
            double division = calculator.divide(a, b);
            double power = calculator.power(a, b);

            // Resultados
            System.out.println("\n=== RESULTADOS RPC ===");
            System.out.println("Multiplicación: " + multiplication);
            System.out.println("División: " + division);
            System.out.println("Potencia: " + power);

        } catch (Exception e) {
            System.out.println("Error en el cliente:");
            e.printStackTrace();
        }
    }
}