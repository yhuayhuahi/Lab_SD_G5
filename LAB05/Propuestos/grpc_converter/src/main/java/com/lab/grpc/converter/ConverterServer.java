package com.lab.grpc.converter;

import java.io.IOException; // IOException para que el main pueda lanzar esta excepción
import java.util.logging.Logger; // logger para imprimir mensajes de estado en el servidor

import com.lab.grpc.converter.ConverterProto.ConvertRequest; // se importan las clases generadas por protoc a partir del .proto
import com.lab.grpc.converter.ConverterProto.ConvertResponse;

import io.grpc.Server; // clases de gRPC para crear el servidor y manejar las conexiones
import io.grpc.ServerBuilder; // configura y construye el servidor gRPC
import io.grpc.stub.StreamObserver; // i para enviar respuestas asíncronas a los clientes desde el servidor

public class ConverterServer {

    private static final Logger logger = Logger.getLogger(ConverterServer.class.getName());

    private static final int PORT = 50051;

    private static final double TASA_SOL_USD = 0.27;

    private Server server;

    public void start() throws IOException {

        server = ServerBuilder
                .forPort(PORT)
                .addService(new ConverterServiceImpl())
                .build()
                .start();

        logger.info("Servidor gRPC iniciado en puerto " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Apagando servidor...");
            ConverterServer.this.stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        ConverterServer server = new ConverterServer();

        server.start();

        server.blockUntilShutdown();
    }

    static class ConverterServiceImpl extends ConverterGrpc.ConverterImplBase {

        @Override
        public void convert(ConvertRequest req, StreamObserver<ConvertResponse> obs) {

            String tipo = req.getType().trim().toLowerCase();

            double valor = req.getValue();

            logger.info("[PETICION] tipo=" + tipo + " valor=" + valor);

            ConvertResponse resp;

            if (Double.isNaN(valor) || Double.isInfinite(valor)) {

                resp = error("Número inválido.");

                obs.onNext(resp);
                obs.onCompleted();
                return;
            }

            switch (tipo) {

                // TEMPERATURA

                case "celsius_fahrenheit":
                    resp = ok(valor * 1.8 + 32,
                            String.format("%.2f °C = %.2f °F", valor, valor * 1.8 + 32));
                    break;

                case "fahrenheit_celsius":
                    resp = ok((valor - 32) / 1.8,
                            String.format("%.2f °F = %.2f °C", valor, (valor - 32) / 1.8));
                    break;

                case "celsius_kelvin":

                    if (valor < -273.15) {
                        resp = error("No existe temperatura menor a -273.15 °C");
                        break;
                    }

                    resp = ok(valor + 273.15,
                            String.format("%.2f °C = %.2f K", valor, valor + 273.15));
                    break;

                case "kelvin_celsius":

                    if (valor < 0) {
                        resp = error("No existe Kelvin negativo");
                        break;
                    }

                    resp = ok(valor - 273.15,
                            String.format("%.2f K = %.2f °C", valor, valor - 273.15));
                    break;

                case "fahrenheit_kelvin":

                    if (valor < -459.67) {
                        resp = error("No existe temperatura menor a -459.67 °F");
                        break;
                    }

                    double fk = (valor - 32) / 1.8 + 273.15;

                    resp = ok(fk,
                            String.format("%.2f °F = %.2f K", valor, fk));
                    break;

                case "kelvin_fahrenheit":

                    if (valor < 0) {
                        resp = error("No existe Kelvin negativo");
                        break;
                    }

                    double kf = (valor - 273.15) * 1.8 + 32;

                    resp = ok(kf,
                            String.format("%.2f K = %.2f °F", valor, kf));
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
                    resp = error("Tipo de conversión desconocido");
            }

            logger.info("[RESPUESTA] " +
                    (resp.getSuccess() ? resp.getDescription() : resp.getErrorMessage()));

            obs.onNext(resp);

            obs.onCompleted();
        }

        private ConvertResponse ok(double resultado, String descripcion) {

            return ConvertResponse.newBuilder()
                    .setResult(resultado)
                    .setDescription(descripcion)
                    .setSuccess(true)
                    .build();
        }

        private ConvertResponse error(String mensaje) {

            return ConvertResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage(mensaje)
                    .build();
        }
    }
}