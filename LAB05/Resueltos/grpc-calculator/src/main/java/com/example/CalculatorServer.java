package com.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class CalculatorServer {

    public static void main(String[] args) throws Exception {

        Server server = ServerBuilder
                .forPort(50051)
                .addService(new CalculatorService())
                .build();

        server.start();

        System.out.println("Servidor iniciado en puerto 50051");

        server.awaitTermination(); // espera señal de terminar
    }
}