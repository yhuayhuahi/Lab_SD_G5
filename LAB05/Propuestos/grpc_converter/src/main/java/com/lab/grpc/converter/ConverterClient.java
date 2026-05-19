package com.lab.grpc.converter;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.lab.grpc.converter.ConverterProto.ConvertRequest;
import com.lab.grpc.converter.ConverterProto.ConvertResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class ConverterClient {

    private static final Logger logger = Logger.getLogger(ConverterClient.class.getName());
    private final ManagedChannel channel;
    private final ConverterGrpc.ConverterBlockingStub stub;

    public ConverterClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        this.stub = ConverterGrpc.newBlockingStub(channel);
        logger.info("Cliente conectado a " + host + ":" + port);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void convertir(String tipo, double valor) {
        ConvertRequest req = ConvertRequest.newBuilder().setType(tipo).setValue(valor).build();
        long inicio = System.currentTimeMillis();
        ConvertResponse resp;
        try {
            resp = stub.convert(req);
        } catch (StatusRuntimeException e) {
            System.out.printf("  [ERROR DE RED] %s%n%n", e.getStatus());
            return;
        }
        long ms = System.currentTimeMillis() - inicio;
        if (resp.getSuccess()) {
            System.out.printf("  OK  %s%n      Resultado: %.6f  |  Tiempo: %d ms%n%n",
                    resp.getDescription(), resp.getResult(), ms);
        } else {
            System.out.printf("  ERROR  %s  |  Tiempo: %d ms%n%n", resp.getErrorMessage(), ms);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ConverterClient cliente = new ConverterClient("localhost", 50051);

        System.out.println("\n===========================================");
        System.out.println("  Cliente gRPC - Sistema de Conversion");
        System.out.println("  Lab 05 Sistemas Distribuidos UNSA 2026");
        System.out.println("===========================================\n");

        System.out.println("--- TEMPERATURA ---");
        cliente.convertir("celsius_fahrenheit", 100.0);
        cliente.convertir("fahrenheit_celsius", 98.6);

        System.out.println("--- MONEDA ---");
        cliente.convertir("soles_dolares", 100.0);
        cliente.convertir("dolares_soles", 50.0);
        cliente.convertir("soles_dolares", -5.0);

        System.out.println("--- DISTANCIA ---");
        cliente.convertir("km_millas", 42.195);
        cliente.convertir("millas_km", 26.219);

        System.out.println("--- TIPO DESCONOCIDO ---");
        cliente.convertir("libras_kg", 70.0);

        cliente.shutdown();
    }
}