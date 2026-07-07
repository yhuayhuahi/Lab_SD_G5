package com.lab.grpc.converter;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConverterServer {

    private static final int PORT = 50051;
    private static final Logger logger = Logger.getLogger(ConverterServer.class.getName());

    private Server server;

    private void start() throws IOException {
        server = ServerBuilder
                .forPort(PORT)
                .addService(new ConverterServiceImpl())
                .build()
                .start();

        logger.info("Servidor gRPC de conversion iniciado en puerto " + PORT);
        logger.info("Servicio disponible: Converter");
        logger.info("Metodos remotos: Convert y GetCatalog");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                ConverterServer.this.stop();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            logger.info("Apagando servidor gRPC de conversion.");
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        ConverterServer converterServer = new ConverterServer();
        converterServer.start();
        converterServer.blockUntilShutdown();
    }

    static class ConverterServiceImpl extends ConverterGrpc.ConverterImplBase {

        @Override
        public void convert(ConvertRequest request, StreamObserver<ConvertResponse> responseObserver) {
            ConvertResponse response;

            try {
                String category = request.getCategory();
                String fromUnit = request.getFromUnit();
                String toUnit = request.getToUnit();
                double value = request.getValue();

                if (category == null || category.isBlank()) {
                    response = convertLegacy(request.getType(), value);
                } else {
                    response = convertByCategory(category, fromUnit, toUnit, value);
                }

                logger.info("[RESPUESTA] " + response.getDescription());
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error al convertir", ex);
                response = error(ex.getMessage());
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getCatalog(CatalogRequest request, StreamObserver<CatalogResponse> responseObserver) {
            CatalogResponse.Builder response = CatalogResponse.newBuilder();

            for (UnitCategory category : UnitCatalog.getCategories()) {
                CategoryInfo.Builder categoryInfo = CategoryInfo.newBuilder()
                        .setCode(category.getCode())
                        .setLabel(category.getLabel())
                        .setDefaultFromUnit(category.getDefaultFromUnit())
                        .setDefaultToUnit(category.getDefaultToUnit());

                for (UnitDefinition unit : category.getUnits()) {
                    categoryInfo.addUnits(UnitInfo.newBuilder()
                            .setCode(unit.getCode())
                            .setLabel(unit.getLabel())
                            .build());
                }

                response.addCategories(categoryInfo.build());
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }

        private ConvertResponse convertByCategory(String category, String fromUnit, String toUnit, double value) {
            logger.info("[PETICION] categoria=" + category + " valor=" + value + " origen=" + fromUnit + " destino=" + toUnit);

            double result = UnitCatalog.convert(category, fromUnit, toUnit, value);

            UnitCategory unitCategory = UnitCatalog.findCategory(category)
                    .orElseThrow(() -> new IllegalArgumentException("Categoria no reconocida: " + category));

            UnitDefinition from = unitCategory.findUnit(fromUnit)
                    .orElseThrow(() -> new IllegalArgumentException("Unidad origen no reconocida: " + fromUnit));

            UnitDefinition to = unitCategory.findUnit(toUnit)
                    .orElseThrow(() -> new IllegalArgumentException("Unidad destino no reconocida: " + toUnit));

            String description = String.format("%.4f %s = %.4f %s", value, from.getLabel(), result, to.getLabel());

            return ConvertResponse.newBuilder()
                    .setSuccess(true)
                    .setResult(result)
                    .setDescription(description)
                    .setCategory(category)
                    .setFromUnit(fromUnit)
                    .setToUnit(toUnit)
                    .build();
        }

        private ConvertResponse convertLegacy(String type, double value) {
            if (type == null) {
                return error("Tipo de conversion no indicado.");
            }

            return switch (type.trim().toLowerCase()) {
                case "celsius_fahrenheit" -> convertByCategory("temperatura", "C", "F", value);
                case "fahrenheit_celsius" -> convertByCategory("temperatura", "F", "C", value);
                case "soles_dolares" -> convertByCategory("moneda", "PEN", "USD", value);
                case "kilometros_millas" -> convertByCategory("longitud", "km", "mi", value);
                case "kg_g" -> convertByCategory("masa", "kg", "g", value);
                case "g_kg" -> convertByCategory("masa", "g", "kg", value);
                default -> error("Tipo de conversion no reconocido: " + type);
            };
        }

        private ConvertResponse error(String message) {
            return ConvertResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage(message == null ? "Error desconocido." : message)
                    .setDescription("Conversion no realizada")
                    .build();
        }
    }
}