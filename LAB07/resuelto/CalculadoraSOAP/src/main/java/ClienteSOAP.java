import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class ClienteSOAP {

    public static void main(String[] args) throws Exception {
        URL url = new URL("http://localhost:8080/calculadora?wsdl");

        QName qname = new QName("http://servicio.soap/", "CalculadoraSOAPService");

        Service service = Service.create(url, qname);

        CalculadoraSOAP calculadora = service.getPort(CalculadoraSOAP.class);

        System.out.println("Suma: " + calculadora.sumar(8, 4));
        System.out.println("Resta: " + calculadora.restar(8, 4));
        System.out.println("Multiplicación: " + calculadora.multiplicar(8, 4));
        System.out.println("División: " + calculadora.dividir(8, 4));
    }
}