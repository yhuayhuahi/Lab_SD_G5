package com.lab.grpc.converter;

import java.io.IOException; 
import java.util.logging.Logger; // logger para imprimir mensajes de estado en el servidor


import io.grpc.Server; // clases de gRPC para crear el servidor y manejar las conexiones
import io.grpc.ServerBuilder; // configura y construye el servidor gRPC
import io.grpc.stub.StreamObserver; // i para enviar respuestas asÃ­ncronas a los clientes desde el servidor

public class ConverterServer {

    private static final Logger logger = Logger.getLogger(ConverterServer.class.getName());

    private static final int PORT = 50051; // puerto en q escucharÃ¡ el servidor

    private static final double TASA_SOL_USD = 0.27; // tasa como valor constante

    private Server server;

    public void start() throws IOException {

        server = ServerBuilder    // construye 
                .forPort(PORT) // forPort lo q hace es configurar el puerto
                .addService(new ConverterServiceImpl()) // addService agrega ConverterServiceImpl que maneja solicitudes
                .build() // construye con esa configuracion
                .start();

        logger.info("Servidor gRPC iniciado en puerto " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> { // cuando se recibe una seÃ±al como ctrlc
            logger.info("Apagando servidor...");
            ConverterServer.this.stop();
        }));
    }

    public void stop() { // stop para apagar
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException { // mantenerlo ejecutandose
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        ConverterServer server = new ConverterServer();

        server.start();

        server.blockUntilShutdown();
    }

    static class ConverterServiceImpl extends ConverterGrpc.ConverterImplBase { // extiende la clase generada por protoc para implementar el servicio definido en el .proto

        @Override
        public void convert(ConvertRequest req, StreamObserver<ConvertResponse> obs) { // maneja solicitudes de conversion
            // streamobserver se usa para enviar la respuesta de forma asÃ­ncrona al cliente, el servidor puede procesar otras solicitudes mientras espera enviar la respuesta a este cliente
            String tipo = req.getType().trim().toLowerCase();

            double valor = req.getValue();

            logger.info("[PETICION] tipo=" + tipo + " valor=" + valor); // log de la solicitud

            ConvertResponse resp; // almacena respuesta

            if (Double.isNaN(valor) || Double.isInfinite(valor)) { 

                resp = error("NÃºmero invÃ¡lido.");

                obs.onNext(resp); // envia la respuesta de error al cliente
                obs.onCompleted(); // completa y termina la comunicacion
                return;
            }

            switch (tipo) {

                // TEMPERATURA

                case "celsius_fahrenheit":
                    resp = ok(valor * 1.8 + 32,
                            String.format("%.2f Â°C = %.2f Â°F", valor, valor * 1.8 + 32));
                    break;

                case "fahrenheit_celsius":
                    resp = ok((valor - 32) / 1.8,
                            String.format("%.2f Â°F = %.2f Â°C", valor, (valor - 32) / 1.8));
                    break;

                case "celsius_kelvin":

                    if (valor < -273.15) { // kelvin no puede ser negativo
                        resp = error("No existe temperatura menor a -273.15 Â°C");
                        break;
                    }

                    resp = ok(valor + 273.15,
                            String.format("%.2f Â°C = %.2f K", valor, valor + 273.15));
                    break;

                case "kelvin_celsius":

                    if (valor < 0) { // kelvin no puede ser negativo
                        resp = error("No existe Kelvin negativo");
                        break;
                    }

                    resp = ok(valor - 273.15,
                            String.format("%.2f K = %.2f Â°C", valor, valor - 273.15));
                    break;

                case "fahrenheit_kelvin":

                    if (valor < -459.67) { // temperatura mÃ¡s baja posible en Fahrenheit
                        resp = error("No existe temperatura menor a -459.67 Â°F");
                        break;
                    }

                    double fk = (valor - 32) / 1.8 + 273.15;

                    resp = ok(fk,
                            String.format("%.2f Â°F = %.2f K", valor, fk));
                    break;

                case "kelvin_fahrenheit":

                    if (valor < 0) { // kelvin no puede ser negativo
                        resp = error("No existe Kelvin negativo");
                        break;
                    }

                    double kf = (valor - 273.15) * 1.8 + 32;

                    resp = ok(kf,
                            String.format("%.2f K = %.2f Â°F", valor, kf));
                    break;

                // MONEDA

                case "soles_dolares":

                    if (valor < 0) {
                        resp = error("Monto negativo no permitido");
                        break;
                    }

                    resp = ok(valor * TASA_SOL_USD,
                            String.format("S/ %.2f = $ %.2f", valor, valor * TASA_SOL_USD));
                    break;

                case "dolares_soles":

                    if (valor < 0) {
                        resp = error("Monto negativo no permitido");
                        break;
                    }

                    resp = ok(valor / TASA_SOL_USD,
                            String.format("$ %.2f = S/ %.2f", valor, valor / TASA_SOL_USD));
                    break;

                // DISTANCIA

                case "km_millas":

                    if (valor < 0) {
                        resp = error("Distancia negativa no permitida");
                        break;
                    }

                    resp = ok(valor * 0.621371,
                            String.format("%.2f km = %.2f millas", valor, valor * 0.621371));
                    break;

                case "millas_km":

                    if (valor < 0) {
                        resp = error("Distancia negativa no permitida");
                        break;
                    }

                    resp = ok(valor / 0.621371,
                            String.format("%.2f millas = %.2f km", valor, valor / 0.621371));
                    break;

                // PESO

                case "kg_g":

                    if (valor < 0) {
                        resp = error("Peso negativo no permitido");
                        break;
                    }

                    resp = ok(valor * 1000,
                            String.format("%.2f kg = %.2f g", valor, valor * 1000));
                    break;

                case "g_kg":

                    if (valor < 0) {
                        resp = error("Peso negativo no permitido");
                        break;
                    }

                    resp = ok(valor / 1000,
                            String.format("%.2f g = %.2f kg", valor, valor / 1000));
                    break;

                case "kg_lb":

                    if (valor < 0) {
                        resp = error("Peso negativo no permitido");
                        break;
                    }

                    resp = ok(valor * 2.20462,
                            String.format("%.2f kg = %.2f lb", valor, valor * 2.20462));
                    break;

                case "lb_kg":

                    if (valor < 0) {
                        resp = error("Peso negativo no permitido");
                        break;
                    }

                    resp = ok(valor / 2.20462,
                            String.format("%.2f lb = %.2f kg", valor, valor / 2.20462));
                    break;

                case "mg_g":

                    if (valor < 0) {
                        resp = error("Peso negativo no permitido");
                        break;
                    }

                    resp = ok(valor / 1000,
                            String.format("%.2f mg = %.2f g", valor, valor / 1000));
                    break;

                case "g_mg":

                    if (valor < 0) {
                        resp = error("Peso negativo no permitido");
                        break;
                    }

                    resp = ok(valor * 1000,
                            String.format("%.2f g = %.2f mg", valor, valor * 1000));
                    break;

                default:
                    resp = error("Tipo de conversiÃ³n desconocido");
            }

            logger.info("[RESPUESTA] " +
                    (resp.getSuccess() ? resp.getDescription() : resp.getErrorMessage()));

            obs.onNext(resp);

            obs.onCompleted();
        }

        private ConvertResponse ok(double resultado, String descripcion) { // respuesta success con el resultado y la descripciÃ³n 

            return ConvertResponse.newBuilder()
                    .setResult(resultado)
                    .setDescription(descripcion)
                    .setSuccess(true)
                    .build();
        }

        private ConvertResponse error(String mensaje) { // respuesta de error sin resultado ni descripcion

            return ConvertResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage(mensaje)
                    .build();
        }
    }
}
