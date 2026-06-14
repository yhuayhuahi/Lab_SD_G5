package soap;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClienteSOAP {
    public static void main(String[] args) throws Exception {

        String url = "http://localhost:8080/conversor";

        // Convertir 30°C a Fahrenheit
        // envelope sirve para envolver el mensaje SOAP y definir los espacios de nombres
        String soapCtoF = """
            <soapenv:Envelope 
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:tns="http://soap/">
              <soapenv:Body>
                <tns:celsiusAFahrenheit>
                  <arg0>30.0</arg0>
                </tns:celsiusAFahrenheit>
              </soapenv:Body>
            </soapenv:Envelope>
            """;

        // Convertir 86°F a Celsius
        String soapFtoC = """
            <soapenv:Envelope
                xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:tns="http://soap/">
              <soapenv:Body>
                <tns:fahrenheitACelsius>
                  <arg0>86.0</arg0>
                </tns:fahrenheitACelsius>
              </soapenv:Body>
            </soapenv:Envelope>
            """;

        HttpClient client = HttpClient.newHttpClient(); // crea un cliente HTTP para enviar las solicitudes SOAP al servicio

        System.out.println("=== 30°C a Fahrenheit ===");
        System.out.println(enviar(client, url, soapCtoF)); // identifica la url a usar y el mensaje SOAP a enviar

        System.out.println("=== 86°F a Celsius ===");
        System.out.println(enviar(client, url, soapFtoC));
    }

    static String enviar(HttpClient client, String url, String soapBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url)) // define la URL del servicio SOAP
            .header("Content-Type", "text/xml; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(soapBody)) // POST para enviar el mensaje SOAP en el cuerpo de la solicitud
            .build(); // construye la solicitud HTTP

        HttpResponse<String> response = client.send( // envía la solicitud y espera la respuesta
            request, HttpResponse.BodyHandlers.ofString() // maneja la respuesta como una cadena de texto
        );
        return response.body(); // devuelve el cuerpo de la respuesta, que contiene el resultado del servicio 
    }
}