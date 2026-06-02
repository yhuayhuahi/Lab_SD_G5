import java.net.URL;
import java.util.Scanner;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class ClienteSOAP {

    public static void main(String[] args) throws Exception {
        ICalculadoraSOAP calculadora = conectarServicio();
        Scanner sc = new Scanner(System.in);

        int opcion;
        do {
            mostrarMenu();
            opcion = leerInt(sc, "Opción: ");

            try {
                ejecutarOperacion(opcion, sc, calculadora);
            } catch (Exception e) {
                System.out.println("Error SOAP: " + e.getMessage());
            }

        } while (opcion != 0);

        sc.close();
    }

    private static ICalculadoraSOAP conectarServicio() throws Exception {
        URL url = new URL("http://localhost:8080/calculadora?wsdl");
        QName qname = new QName("http://servicio.soap/", "CalculadoraSOAPService");

        Service service = Service.create(url, qname);
        return service.getPort(ICalculadoraSOAP.class);
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

    private static void ejecutarOperacion(int opcion, Scanner sc, ICalculadoraSOAP calculadora) {
        switch (opcion) {
            case 1: {
                int a = leerInt(sc, "A: ");
                int b = leerInt(sc, "B: ");
                imprimir(calculadora.sumar(a, b));
                break;
            }

            case 2: {
                int a = leerInt(sc, "A: ");
                int b = leerInt(sc, "B: ");
                imprimir(calculadora.restar(a, b));
                break;
            }

            case 3: {
                int a = leerInt(sc, "A: ");
                int b = leerInt(sc, "B: ");
                imprimir(calculadora.multiplicar(a, b));
                break;
            }

            case 4: {
                double a = leerDouble(sc, "A: ");
                double b = leerDouble(sc, "B: ");

                if (b == 0) {
                    System.out.println("Error: no se puede dividir entre cero.");
                    break;
                }

                imprimir(calculadora.dividir(a, b));
                break;
            }

            case 5: {
                double base = leerDouble(sc, "Base: ");
                double exponente = leerDouble(sc, "Exponente: ");
                imprimir(calculadora.potencia(base, exponente));
                break;
            }

            case 6: {
                double numero = leerDouble(sc, "Número: ");

                if (numero < 0) {
                    System.out.println("Error: no existe raíz cuadrada real para números negativos.");
                    break;
                }

                imprimir(calculadora.raizCuadrada(numero));
                break;
            }

            case 7: {
                int n = leerInt(sc, "Número: ");

                if (n < 0) {
                    System.out.println("Error: el factorial no está definido para números negativos.");
                    break;
                }

                imprimir(calculadora.factorial(n));
                break;
            }

            case 0:
                System.out.println("Cliente finalizado.");
                break;

            default:
                System.out.println("Opción no válida.");
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

            System.out.println("Error: ingrese un número entero válido.");
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

            System.out.println("Error: ingrese un número válido.");
            sc.nextLine();
        }
    }

    private static void imprimir(double resultado) {
        System.out.println("Resultado: " + resultado);
    }
}