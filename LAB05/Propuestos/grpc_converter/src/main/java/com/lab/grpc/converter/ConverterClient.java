package com.lab.grpc.converter;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.lab.grpc.converter.ConverterProto.ConvertRequest;
import com.lab.grpc.converter.ConverterProto.ConvertResponse;

import io.grpc.ManagedChannel;      // para manejar conexion con el servidor gRPC
import io.grpc.ManagedChannelBuilder; // construye el canal
import io.grpc.StatusRuntimeException; // maneja errores de red

public class ConverterClient {

    private static final Logger logger = Logger.getLogger(ConverterClient.class.getName());
    private final ManagedChannel channel; // para la conexion con el servidor gRPC
    private final ConverterGrpc.ConverterBlockingStub stub; // servicio bloqueante para hacer llamadas al servidor 

    public ConverterClient(String host, int port) { 
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build(); // crea el canal sin seguridad (plaintext) para conectarse al servidor gRPC
        this.stub = ConverterGrpc.newBlockingStub(channel); // stub bloqueante para hacer llamadas
        logger.info("Cliente conectado a " + host + ":" + port); // log
    }

    public void shutdown() throws InterruptedException { // cierra el canal
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void convertir(String tipo, double valor) { // para enviar solicitud de conversion 
        ConvertRequest req = ConvertRequest.newBuilder().setType(tipo).setValue(valor).build(); // 
        long inicio = System.currentTimeMillis(); // para medir el tiempo de respuesta del servidor
        ConvertResponse resp; // respuesta del servidor
        try {
            resp = stub.convert(req);   // stub bloqueante hace la llamada al servidor con la solicitud de conversion
        } catch (StatusRuntimeException e) {
            System.out.printf("  [ERROR DE RED] %s%n%n", e.getStatus()); 
            return;
        }
        long ms = System.currentTimeMillis() - inicio; // calcula el tiempo que tardó la respuesta del servidor
        if (resp.getSuccess()) {    // si la conversion fue exitosa muestra el resultado y el tiempo
            System.out.printf("  OK  %s%n      Resultado: %.6f  |  Tiempo: %d ms%n%n",
                    resp.getDescription(), resp.getResult(), ms);            // muestra descripcion resultado tiempo
        } else {
            System.out.printf("  ERROR  %s  |  Tiempo: %d ms%n%n", resp.getErrorMessage(), ms);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ConverterClient cliente = new ConverterClient("localhost", 50051); // crea el cliente y se conecta al servidor gRPC en localhost:50051

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