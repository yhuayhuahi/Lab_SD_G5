package com.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CalculatorClient {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder // crea un canal de comunicación con el servidor gRPC
                .forAddress("localhost", 50051) 
                .usePlaintext() // sin seguridad para simplificar 
                .build(); // construye el canal

        CalculatorGrpc.CalculatorBlockingStub stub =
                CalculatorGrpc.newBlockingStub(channel); // stub bloqueante para hacer llamadas al servidor, bloqueante porque el cliente esperará la respuesta del servidor antes de continuar

        Request request = Request.newBuilder() // construye la solicitud con los números a operar
                .setA(8)
                .setB(4)
                .build();

        Response response = stub.sum(request);

        System.out.println("Resultado: " + response.getResult());

        channel.shutdown();
    }
}