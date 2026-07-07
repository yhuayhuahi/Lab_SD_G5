package com.lab.grpc.converter;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConverterServer {

    private static final Logger logger = Logger.getLogger(ConverterServer.class.getName());
    private static final int PORT = 50051;

    private Server server;

    public void start() throws IOException {
        server = ServerBuilder
                .forPort(PORT)
                .addService(new ConverterServiceImpl())
                .build()
                .start();

        logger.info("Servidor gRPC iniciado en puerto " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Apagando servidor gRPC");
            ConverterServer.this.stop();
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    static class ConverterServiceImpl extends ConverterGrpc.ConverterImplBase {

        @Override
        public void convert(ConvertRequest request, StreamObserver<ConvertResponse> responseObserver) {
            ConvertResponse response;

            try {
                String category = normalize(request.getCategory());
                String fromUnit = normalize(request.getFromUnit());
                String toUnit = normalize(request.getToUnit());
                double value = request.getValue();

                if (category.isBlank() || fromUnit.isBlank() || toUnit.isBlank()) {
                    response = convertLegacy(request.getType(), value);
                } else {
                    response = convertByCategory(category, fromUnit, toUnit, value);
                }
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error al convertir", ex);
                response = error(ex.getMessage());
            }

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void getCatalog(CatalogRequest request, StreamObserver<CatalogResponse> responseObserver) {
            CatalogResponse.Builder catalogBuilder = CatalogResponse.newBuilder();

            for (UnitCategory category : UnitCatalog.getCategories()) {
                CategoryInfo.Builder categoryBuilder = CategoryInfo.newBuilder()
                        .setCode(category.getCode())
                        .setLabel(category.getLabel())
                        .setDefaultFromUnit(category.getDefaultFromUnit())
                        .setDefaultToUnit(category.getDefaultToUnit());

                for (UnitDefinition unit : category.getUnits()) {
                    categoryBuilder.addUnits(UnitInfo.newBuilder()
                            .setCode(unit.getCode())
                            .setLabel(unit.getLabel())
                            .build());
                }

                catalogBuilder.addCategories(categoryBuilder.build());
            }

            responseObserver.onNext(catalogBuilder.build());
            responseObserver.onCompleted();
        }

        private ConvertResponse convertByCategory(String category, String fromUnit, String toUnit, double value) {
            logger.info("[PETICION] categoria=" + category + " valor=" + value + " de=" + fromUnit + " a=" + toUnit);

            double result = UnitCatalog.convert(category, fromUnit, toUnit, value);
            String description = UnitCatalog.describe(category, fromUnit, toUnit, value, result);

            logger.info("[RESPUESTA] " + description);

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
            String legacyType = normalize(type);

            switch (legacyType) {
                case "celsius_fahrenheit":
                    return convertByCategory("temperatura", "C", "F", value);
                case "fahrenheit_celsius":
                    return convertByCategory("temperatura", "F", "C", value);
                case "celsius_kelvin":
                    return convertByCategory("temperatura", "C", "K", value);
                case "kelvin_celsius":
                    return convertByCategory("temperatura", "K", "C", value);
                case "fahrenheit_kelvin":
                    return convertByCategory("temperatura", "F", "K", value);
                case "kelvin_fahrenheit":
                    return convertByCategory("temperatura", "K", "F", value);
                case "soles_dolares":
                    return convertByCategory("moneda", "PEN", "USD", value);
                case "dolares_soles":
                    return convertByCategory("moneda", "USD", "PEN", value);
                case "km_millas":
                    return convertByCategory("longitud", "km", "mi", value);
                case "millas_km":
                    return convertByCategory("longitud", "mi", "km", value);
                case "kg_g":
                    return convertByCategory("masa", "kg", "g", value);
                case "g_kg":
                    return convertByCategory("masa", "g", "kg", value);
                case "kg_lb":
                    return convertByCategory("masa", "kg", "lb", value);
                case "lb_kg":
                    return convertByCategory("masa", "lb", "kg", value);
                case "mg_g":
                    return convertByCategory("masa", "mg", "g", value);
                case "g_mg":
                    return convertByCategory("masa", "g", "mg", value);
                default:
                    throw new IllegalArgumentException("Tipo de conversion no reconocido: " + legacyType);
            }
        }

        private ConvertResponse error(String message) {
            return ConvertResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage(message == null ? "Error desconocido" : message)
                    .build();
        }

        private String normalize(String value) {
            return value == null ? "" : value.trim();
        }
    }

    public static void main(String[] args) throws Exception {
        ConverterServer server = new ConverterServer();
        server.start();
        server.blockUntilShutdown();
    }
}