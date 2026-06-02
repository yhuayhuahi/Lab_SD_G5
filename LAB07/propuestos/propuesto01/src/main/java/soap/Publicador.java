package soap;

import jakarta.xml.ws.Endpoint;

public class Publicador {
    public static void main(String[] args) {
        Endpoint.publish("http://localhost:8080/conversor", new ConversorSOAP());
        System.out.println("Conversor activo:  http://localhost:8080/conversor?wsdl");

        Endpoint.publish("http://localhost:8080/ventas", new VentasSOAP());
        System.out.println("Ventas activo:     http://localhost:8080/ventas?wsdl");
    }
}