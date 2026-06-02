package soap;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.sun.net.httpserver.HttpServer;

public class ProxyServidor {
public static void main(String[] args) throws Exception {

    HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

    // Sirve el archivo HTML
    server.createContext("/", exchange -> {
        try {
            File f = new File("src/main/resources/index.html");
            byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    });

    // Proxy hacia el servicio SOAP de ventas
    server.createContext("/proxy/ventas", exchange -> {
        try {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/ventas"))
                .header("Content-Type", "text/xml; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            byte[] respBytes = resp.body().getBytes();

            exchange.getResponseHeaders().add("Content-Type", "text/xml; charset=utf-8");
            exchange.sendResponseHeaders(200, respBytes.length);
            exchange.getResponseBody().write(respBytes);
            exchange.getResponseBody().close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    });

    server.start();
    System.out.println("Proxy + UI activo en: http://localhost:8081");
}
}