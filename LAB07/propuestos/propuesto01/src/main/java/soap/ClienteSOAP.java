package soap;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClienteSOAP {
    public static void main(String[] args) throws Exception {

        String url = "http://localhost:8080/conversor";

        // Convertir 30°C a Fahrenheit
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

        HttpClient client = HttpClient.newHttpClient();

        System.out.println("=== 30°C a Fahrenheit ===");
        System.out.println(enviar(client, url, soapCtoF));

        System.out.println("=== 86°F a Celsius ===");
        System.out.println(enviar(client, url, soapFtoC));
    }

    static String enviar(HttpClient client, String url, String soapBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "text/xml; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(soapBody))
            .build();

        HttpResponse<String> response = client.send(
            request, HttpResponse.BodyHandlers.ofString()
        );
        return response.body();
    }
}