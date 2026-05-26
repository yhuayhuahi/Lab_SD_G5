package com.example;

import io.grpc.stub.StreamObserver;

public class CalculatorService extends CalculatorGrpc.CalculatorImplBase { // extiende la clase generada por gRPC p
    @Override
    public void sum(Request req, StreamObserver<Response> responseObserver) { // implementa el método sum definido en el proto, recibe una solicitud con dos números y un StreamObserver para enviar la respuesta al cliente

        int result = req.getA() + req.getB();

        Response response = Response.newBuilder()
                .setResult(result)
                .build();

        responseObserver.onNext(response); // envia la respuesta al cliente
        responseObserver.onCompleted(); // indica q se completo
    }
}