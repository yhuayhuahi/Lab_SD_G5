import javax.xml.ws.Endpoint; // para publicar en URL q indicamos

public class Publicador {

  public static void main(String[] args) {
    Endpoint.publish( // 
      "http://localhost:8080/calculadora",
      new CalculadoraSOAP() // instancia la clase que implementa el servicio 
    );
    System.out.println("Servicio SOAP activo");
  }
}
