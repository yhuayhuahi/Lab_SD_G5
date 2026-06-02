import java.net.URL;
import java.util.Scanner;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class ClienteSOAP {

  public static void main(String[] args) throws Exception {
    URL url = new URL("http://localhost:8080/calculadora?wsdl");
    QName qname = new QName("http://servicio.soap/", "CalculadoraSOAPService");

    Service service = Service.create(url, qname);
    ICalculadoraSOAP calculadora = service.getPort(ICalculadoraSOAP.class);

    Scanner sc = new Scanner(System.in);
    int opcion;

    do {
      System.out.println("\n===== CLIENTE SOAP - CALCULADORA =====");
      System.out.println("1. Sumar");
      System.out.println("2. Restar");
      System.out.println("3. Multiplicar");
      System.out.println("4. Dividir");
      System.out.println("5. Potencia");
      System.out.println("6. Raíz cuadrada");
      System.out.println("7. Factorial");
      System.out.println("0. Salir");
      System.out.print("Opción: ");

      opcion = leerInt(sc, "Opción: ");

      try {
        switch (opcion) {
          case 1:
            System.out.println("Resultado: " + calculadora.sumar(leerInt(sc, "A: "), leerInt(sc, "B: ")));
            break;

          case 2:
            System.out.println("Resultado: " + calculadora.restar(leerInt(sc, "A: "), leerInt(sc, "B: ")));
            break;

          case 3:
            System.out.println("Resultado: " + calculadora.multiplicar(leerInt(sc, "A: "), leerInt(sc, "B: ")));
            break;

          case 4:
            System.out.println("Resultado: " + calculadora.dividir(leerDouble(sc, "A: "), leerDouble(sc, "B: ")));
            break;

          case 5:
            System.out
                .println("Resultado: " + calculadora.potencia(leerDouble(sc, "Base: "), leerDouble(sc, "Exponente: ")));
            break;

          case 6:
            System.out.println("Resultado: " + calculadora.raizCuadrada(leerDouble(sc, "Número: ")));
            break;

          case 7:
            System.out.println("Resultado: " + calculadora.factorial(leerInt(sc, "Número: ")));
            break;

          case 0:
            System.out.println("Cliente finalizado.");
            break;

          default:
            System.out.println("Opción no válida.");
        }
      } catch (Exception e) {
        System.out.println("Error enviado por el servicio SOAP: operación no válida.");
      }

    } while (opcion != 0);

    sc.close();
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
}