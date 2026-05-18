package com.example;

import io.grpc.stub.StreamObserver;

public class CalculatorService extends CalculatorGrpc.CalculatorImplBase {

    @Override
    public void sum(Request req, StreamObserver<Response> responseObserver) {

        int result = req.getA() + req.getB();

        Response response = Response.newBuilder()
                .setResult(result)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}