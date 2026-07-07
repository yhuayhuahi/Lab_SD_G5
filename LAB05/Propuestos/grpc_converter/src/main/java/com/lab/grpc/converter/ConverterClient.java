package com.lab.grpc.converter;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

public class ConverterClient {

    private final ManagedChannel channel;
    private final ConverterGrpc.ConverterBlockingStub stub;

    public ConverterClient(String host, int port) {
        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        this.stub = ConverterGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void printCatalog() {
        CatalogResponse catalog = stub.getCatalog(CatalogRequest.newBuilder().build());

        System.out.println("=== CATALOGO DISPONIBLE ===");
        for (CategoryInfo category : catalog.getCategoriesList()) {
            System.out.println("- " + category.getLabel() + " (" + category.getCode() + ")");
        }
        System.out.println();
    }

    public void convert(String category, String fromUnit, String toUnit, double value) {
        ConvertRequest request = ConvertRequest.newBuilder()
                .setCategory(category)
                .setFromUnit(fromUnit)
                .setToUnit(toUnit)
                .setValue(value)
                .build();

        long start = System.nanoTime();

        try {
            ConvertResponse response = stub.convert(request);
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            if (response.getSuccess()) {
                System.out.println("[OK] " + response.getDescription() + " | " + elapsedMs + " ms");
            } else {
                System.out.println("[ERROR] " + response.getErrorMessage() + " | " + elapsedMs + " ms");
            }
        } catch (StatusRuntimeException ex) {
            System.out.println("[ERROR DE RED] " + ex.getStatus());
        }
    }

    public static void main(String[] args) throws Exception {
        ConverterClient client = new ConverterClient("localhost", 50051);

        try {
            client.printCatalog();

            System.out.println("=== PRUEBAS GPRC ===");
            client.convert("temperatura", "C", "F", 100);
            client.convert("longitud", "km", "mi", 42.195);
            client.convert("masa", "kg", "lb", 70);
            client.convert("area", "ha", "m2", 1.5);
            client.convert("volumen", "l", "m3", 250);
            client.convert("energia", "kWh", "J", 2);
            client.convert("moneda", "PEN", "USD", 100);
            client.convert("presion", "atm", "Pa", 1);

            System.out.println();
            System.out.println("=== PRUEBAS DE VALIDACION ===");
            client.convert("masa", "kg", "g", -5);
            client.convert("temperatura", "K", "C", -10);
        } finally {
            client.shutdown();
        }
    }
}