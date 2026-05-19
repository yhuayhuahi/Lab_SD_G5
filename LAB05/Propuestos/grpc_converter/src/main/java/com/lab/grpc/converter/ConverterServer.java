package com.lab.grpc.converter;

import java.io.IOException;
import java.util.logging.Logger;

import com.lab.grpc.converter.ConverterProto.ConvertRequest;
import com.lab.grpc.converter.ConverterProto.ConvertResponse;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class ConverterServer {

    private static final Logger logger = Logger.getLogger(ConverterServer.class.getName());
    private static final int PORT = 50051;
    private static final double TASA_SOL_USD = 0.27;
    private Server server;

    public void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(new ConverterServiceImpl())
                .build().start();
        logger.info("Servidor gRPC listo en puerto " + PORT);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Apagando servidor...");
            ConverterServer.this.stop();
        }));
    }

    public void stop() {
        if (server != null) server.shutdown();
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) server.awaitTermination();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ConverterServer srv = new ConverterServer();
        srv.start();
        srv.blockUntilShutdown();
    }

    static class ConverterServiceImpl extends ConverterGrpc.ConverterImplBase {

        @Override
        public void convert(ConvertRequest req, StreamObserver<ConvertResponse> obs) {
            String tipo = req.getType().trim().toLowerCase();
            double valor = req.getValue();
            logger.info("[PETICION] tipo=" + tipo + "  valor=" + valor);

            ConvertResponse resp;
            switch (tipo) {
                case "celsius_fahrenheit":
                    resp = ok(valor * 1.8 + 32,
                        String.format("%.2f C -> %.2f F", valor, valor * 1.8 + 32));
                    break;
                case "fahrenheit_celsius":
                    resp = ok((valor - 32) / 1.8,
                        String.format("%.2f F -> %.2f C", valor, (valor - 32) / 1.8));
                    break;
                case "soles_dolares":
                    if (valor < 0) { resp = error("Monto negativo no permitido."); break; }
                    resp = ok(valor * TASA_SOL_USD,
                        String.format("S/ %.2f -> $ %.2f", valor, valor * TASA_SOL_USD));
                    break;
                case "dolares_soles":
                    if (valor < 0) { resp = error("Monto negativo no permitido."); break; }
                    resp = ok(valor / TASA_SOL_USD,
                        String.format("$ %.2f -> S/ %.2f", valor, valor / TASA_SOL_USD));
                    break;
                case "km_millas":
                    if (valor < 0) { resp = error("Distancia negativa no permitida."); break; }
                    resp = ok(valor * 0.621371,
                        String.format("%.3f km -> %.4f millas", valor, valor * 0.621371));
                    break;
                case "millas_km":
                    if (valor < 0) { resp = error("Distancia negativa no permitida."); break; }
                    resp = ok(valor / 0.621371,
                        String.format("%.4f millas -> %.3f km", valor, valor / 0.621371));
                    break;
                default:
                    resp = error("Tipo desconocido: " + tipo);
            }

            logger.info("[RESPUESTA] " + resp.getDescription());
            obs.onNext(resp);
            obs.onCompleted();
        }

        private ConvertResponse ok(double resultado, String descripcion) {
            return ConvertResponse.newBuilder()
                    .setResult(resultado).setDescription(descripcion).setSuccess(true).build();
        }

        private ConvertResponse error(String mensaje) {
            return ConvertResponse.newBuilder()
                    .setSuccess(false).setErrorMessage(mensaje).build();
        }
    }
}