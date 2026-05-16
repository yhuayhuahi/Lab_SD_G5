package org.example;

import org.example.CalculatorProto.OperationRequest;
import org.example.CalculatorProto.OperationResponse;
// import org.example.CalculatorServiceGrpc;
import org.example.CalculatorServiceGrpc.CalculatorServiceImplBase;

import io.grpc.stub.StreamObserver;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ServerGrpc {
  private Server server;

  private void start() throws Exception {
    int port = 8080;

    server = ServerBuilder.forPort(port).addService(new CalculatorServiceImpl()).build().start();

    System.out.println("Servidor escuchando en el puerto:" + port);
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

  static class CalculatorServiceImpl extends CalculatorServiceImplBase {
    @Override
    public void add(OperationRequest request, StreamObserver<OperationResponse> responseObserver) {
      double result = request.getNum1() + request.getNum2();
      
      OperationResponse response = OperationResponse.newBuilder().setResult(result).build();
      
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }

    public void sub(OperationRequest request, StreamObserver<OperationResponse> responseObserver) {
      double result = request.getNum1() - request.getNum2();
      
      OperationResponse response = OperationResponse.newBuilder().setResult(result).build();
      
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }

    public void mul(OperationRequest request, StreamObserver<OperationResponse> responseObserver) {
      double result = request.getNum1() * request.getNum2();
      
      OperationResponse response = OperationResponse.newBuilder().setResult(result).build();
      
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }

    public void div(OperationRequest request, StreamObserver<OperationResponse> responseObserver) {
      double result = request.getNum1() / request.getNum2();
      
      OperationResponse response = OperationResponse.newBuilder().setResult(result).build();
      
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
  }

  public static void main(String[] args) throws Exception {
    final ServerGrpc server = new ServerGrpc();
    server.start();

    // Agregar hook para shutdown graceful
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.err.println("Cerrando servidor...");
      server.stop();
      System.err.println("Servidor cerrado.");
    }));

    server.blockUntilShutdown();
  }
}
