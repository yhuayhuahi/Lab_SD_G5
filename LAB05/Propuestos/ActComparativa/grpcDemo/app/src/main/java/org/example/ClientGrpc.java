package org.example;

import java.util.concurrent.TimeUnit;

import org.example.CalculatorProto.*;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ClientGrpc {
  private final ManagedChannel channel;
  private final CalculatorServiceGrpc.CalculatorServiceBlockingStub blockingStub;

  public ClientGrpc(String host, int port) {
    // Crear canal de comunicación
    this.channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()  // Sin SSL (solo para desarrollo)
            .build();
    
    // Crear stub bloqueante (para Unary y Server Streaming)
    this.blockingStub = CalculatorServiceGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  public void add(double num1, double num2) {
    OperationRequest request = OperationRequest.newBuilder().setNum1(num1).setNum2(num2).build();

    OperationResponse response = blockingStub.add(request);

    System.out.println("Resultado de la suma: " + num1 + " + " + num2 + " = " + response.getResult() + "\n");
  }

  public void sub(double num1, double num2) {
    OperationRequest request = OperationRequest.newBuilder().setNum1(num1).setNum2(num2).build();

    OperationResponse response = blockingStub.sub(request);

    System.out.println("Resultado de la resta: " + num1 + " - " + num2 + " = " + response.getResult() + "\n");
  }

  public void mul(double num1, double num2) {
    OperationRequest request = OperationRequest.newBuilder().setNum1(num1).setNum2(num2).build();

    OperationResponse response = blockingStub.mul(request);

    System.out.println("Resultado de la multiplicacion: " + num1 + " * " + num2 + " = " + response.getResult() + "\n");
  }

  public void div(double num1, double num2) {
    OperationRequest request = OperationRequest.newBuilder().setNum1(num1).setNum2(num2).build();

    OperationResponse response = blockingStub.div(request);

    System.out.println("Resultado de la division: " + num1 + " / " + num2 + " = " + response.getResult() + "\n");
  }

  public static void main(String[] args) throws Exception {
    ClientGrpc client = new ClientGrpc("localhost", 8080);
    
    try {
      System.out.println("Realizando operaciones...");

      int initialTime = (int) System.currentTimeMillis();

      System.out.println("Suma:");
      client.add(10, 5);

      System.out.println("Resta:");
      client.sub(10, 5);

      System.out.println("Multiplicacion:");
      client.mul(10, 5);

      System.out.println("Division:");
      client.div(10, 5);

      int finalTime = (int) System.currentTimeMillis();

      System.out.println("Tiempo total de ejecucion: " + (finalTime - initialTime) + " ms");


    } finally {
      client.shutdown();
    }
  }
}
