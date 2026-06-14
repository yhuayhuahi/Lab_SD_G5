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
    // crea un servidor HTTP en el puerto 8081 para servir la interfaz web y actuar como proxy hacia el servicio SOAP de ventas
    HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

    // Sirve el archivo HTML
    server.createContext("/", exchange -> {
        try {
            File f = new File("src/main/resources/index.html"); // 
            byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath()); // bytes es el contenido del archivo HTML leído como un arreglo de bytes
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
        try { // usa CORS para q la interfaz haga solicitudes al proxy sin problemas de seguridad que podrian darse al ser dominios diferentes (localhost:8081 para la interfaz y localhost:8080 para el servicio SOAP)
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
           // si la solicitud es OPTIONS, responde con 204 No Content para indicar que el proxy acepta las solicitudes POST desde cualquier origen
            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            // crea un cliente 
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder() // define la solicitud http 
                .uri(URI.create("http://localhost:8080/ventas")) // URL del servicio SOAP de ventas
                .header("Content-Type", "text/xml; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body)) // envia el cuerpo de la solicitud recibida desde la interfaz web al servicio SOAP de ventas
                .build();
            // resp es la respuesta del servicio SOAP de ventas y la reenvia a la interfaz que hizo la solicitud al proxy
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            byte[] respBytes = resp.body().getBytes();
            // reenvia la respuesta del servicio SOAP de ventas a la interfaz
            exchange.getResponseHeaders().add("Content-Type", "text/xml; charset=utf-8");
            exchange.sendResponseHeaders(200, respBytes.length);
            exchange.getResponseBody().write(respBytes);
            exchange.getResponseBody().close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    });

    server.start(); // inicia el servidor HTTP
    System.out.println("Proxy + UI activo en: http://localhost:8081"); // la interfaz web estará disponible en esta dirección, y el proxy reenviará las solicitudes al servicio SOAP de ventas en localhost:8080
}
}