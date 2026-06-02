import java.net.URI; //
import java.net.URL; // para manejar URLs
import java.util.Scanner;

import javax.xml.namespace.QName; // qname 
import javax.xml.ws.Service; // consumir el servicio SOAP a través de su WSDL
// q es un XML accesible desde la URL

public class ClienteSOAP {

    public static void main(String[] args) throws Exception {
        // aqui la interfaz define operaciones
        ICalculadoraSOAP calculadora = conectarServicio(); 

        try (Scanner sc = new Scanner(System.in)) {
            int opcion; 

            do {
                mostrarMenu(); 
                opcion = leerInt(sc, "Opción: ");
                ejecutar(opcion, sc, calculadora);
            } while (opcion != 0);
        }
    }

    private static ICalculadoraSOAP conectarServicio() throws Exception {
        // create para crear un servicio SOAP y toURL para acceder
        URL url = URI.create("http://localhost:8080/calculadora?wsdl").toURL();
        QName qname = new QName("http://servicio.soap/", "CalculadoraSOAPService");

        Service service = Service.create(url, qname); // crea el servicio
        return service.getPort(ICalculadoraSOAP.class); // obtiene el puerto
    }

    private static void mostrarMenu() {
        System.out.println("\n===== CLIENTE SOAP - CALCULADORA =====");
        System.out.println("1. Sumar");
        System.out.println("2. Restar");
        System.out.println("3. Multiplicar");
        System.out.println("4. Dividir");
        System.out.println("5. Potencia");
        System.out.println("6. Raíz cuadrada");
        System.out.println("7. Factorial");
        System.out.println("0. Salir");
    }

    // recibe la interfaz para ejecutar las operaciones 
    private static void ejecutar(int opcion, Scanner sc, ICalculadoraSOAP c) {
        try {
            switch (opcion) {
                case 1 -> {
                    int a = leerInt(sc, "A: ");
                    int b = leerInt(sc, "B: ");
                    mostrar("SUMA", a + " + " + b, c.sumar(a, b));
                }

                case 2 -> {
                    int a = leerInt(sc, "A: ");
                    int b = leerInt(sc, "B: ");
                    mostrar("RESTA", a + " - " + b, c.restar(a, b));
                }

                case 3 -> {
                    int a = leerInt(sc, "A: ");
                    int b = leerInt(sc, "B: ");
                    mostrar("MULTIPLICACIÓN", a + " × " + b, c.multiplicar(a, b));
                }

                case 4 -> {
                    double a = leerDouble(sc, "A: ");
                    double b = leerDouble(sc, "B: ");

                    if (b == 0) {
                        System.out.println("No se puede dividir entre cero.");
                        return;
                    }

                    mostrar("DIVISIÓN", f(a) + " ÷ " + f(b), c.dividir(a, b));
                }

                case 5 -> {
                    double base = leerDouble(sc, "Base: ");
                    double exponente = leerDouble(sc, "Exponente: ");
                    mostrar("POTENCIA", f(base) + "^" + f(exponente), c.potencia(base, exponente));
                }

                case 6 -> {
                    double n = leerDouble(sc, "Número: ");

                    if (n < 0) {
                        System.out.println("No existe raíz cuadrada real para números negativos.");
                        return;
                    }

                    mostrar("RAÍZ CUADRADA", "sqrt(" + f(n) + ")", c.raizCuadrada(n));
                }

                case 7 -> {
                    int n = leerInt(sc, "Número: ");

                    if (n < 0) {
                        System.out.println("El factorial no está definido para números negativos.");
                        return;
                    }

                    mostrar("FACTORIAL", n + "!", c.factorial(n));
                }

                case 0 -> System.out.println("Cliente finalizado.");

                default -> System.out.println("Opción no válida.");
            }

        } catch (Exception e) {
            System.out.println("Error SOAP: " + e.getMessage());
        }
    }

    private static int leerInt(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);

            if (sc.hasNextInt()) {
                int valor = sc.nextInt();
                sc.nextLine();
                return valor;
            }

            System.out.println("Ingrese un número entero válido.");
            sc.nextLine();
        }
    }

    private static double leerDouble(Scanner sc, String mensaje) {
        while (true) {
            System.out.print(mensaje);

            if (sc.hasNextDouble()) {
                double valor = sc.nextDouble();
                sc.nextLine();
                return valor;
            }

            System.out.println("Ingrese un número válido.");
            sc.nextLine();
        }
    }
     // formateo
    private static void mostrar(String operacion, String expresion, double resultado) {
        System.out.println("\n--- " + operacion + " DE " + expresion + " ---");
        System.out.println(expresion + " = " + f(resultado));
    }
      // quita decimales si es int
    private static String f(double numero) { 
        if (numero == Math.rint(numero)) {
            return String.valueOf((long) numero);
        }
        return String.valueOf(numero);
    }
}