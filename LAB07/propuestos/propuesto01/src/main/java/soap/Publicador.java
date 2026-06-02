package soap;

import jakarta.xml.ws.Endpoint;

public class Publicador {
    public static void main(String[] args) {
        String url = "http://localhost:8080/conversor";
        Endpoint.publish(url, new ConversorSOAP());
        System.out.println("Servicio SOAP activo en: " + url);
        System.out.println("WSDL disponible en: " + url + "?wsdl");
    }
}