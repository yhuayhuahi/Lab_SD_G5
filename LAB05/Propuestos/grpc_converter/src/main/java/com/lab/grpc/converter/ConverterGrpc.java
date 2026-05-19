package com.lab.grpc.converter;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: converter.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ConverterGrpc {

  private ConverterGrpc() {}

  public static final java.lang.String SERVICE_NAME = "Converter";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.lab.grpc.converter.ConverterProto.ConvertRequest,
      com.lab.grpc.converter.ConverterProto.ConvertResponse> getConvertMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Convert",
      requestType = com.lab.grpc.converter.ConverterProto.ConvertRequest.class,
      responseType = com.lab.grpc.converter.ConverterProto.ConvertResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.lab.grpc.converter.ConverterProto.ConvertRequest,
      com.lab.grpc.converter.ConverterProto.ConvertResponse> getConvertMethod() {
    io.grpc.MethodDescriptor<com.lab.grpc.converter.ConverterProto.ConvertRequest, com.lab.grpc.converter.ConverterProto.ConvertResponse> getConvertMethod;
    if ((getConvertMethod = ConverterGrpc.getConvertMethod) == null) {
      synchronized (ConverterGrpc.class) {
        if ((getConvertMethod = ConverterGrpc.getConvertMethod) == null) {
          ConverterGrpc.getConvertMethod = getConvertMethod =
              io.grpc.MethodDescriptor.<com.lab.grpc.converter.ConverterProto.ConvertRequest, com.lab.grpc.converter.ConverterProto.ConvertResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Convert"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.lab.grpc.converter.ConverterProto.ConvertRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.lab.grpc.converter.ConverterProto.ConvertResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ConverterMethodDescriptorSupplier("Convert"))
              .build();
        }
      }
    }
    return getConvertMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ConverterStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ConverterStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ConverterStub>() {
        @java.lang.Override
        public ConverterStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ConverterStub(channel, callOptions);
        }
      };
    return ConverterStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ConverterBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ConverterBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ConverterBlockingStub>() {
        @java.lang.Override
        public ConverterBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ConverterBlockingStub(channel, callOptions);
        }
      };
    return ConverterBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ConverterFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ConverterFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ConverterFutureStub>() {
        @java.lang.Override
        public ConverterFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ConverterFutureStub(channel, callOptions);
        }
      };
    return ConverterFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void convert(com.lab.grpc.converter.ConverterProto.ConvertRequest request,
        io.grpc.stub.StreamObserver<com.lab.grpc.converter.ConverterProto.ConvertResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getConvertMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service Converter.
   */
  public static abstract class ConverterImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return ConverterGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service Converter.
   */
  public static final class ConverterStub
      extends io.grpc.stub.AbstractAsyncStub<ConverterStub> {
    private ConverterStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ConverterStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ConverterStub(channel, callOptions);
    }

    /**
     */
    public void convert(com.lab.grpc.converter.ConverterProto.ConvertRequest request,
        io.grpc.stub.StreamObserver<com.lab.grpc.converter.ConverterProto.ConvertResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getConvertMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service Converter.
   */
  public static final class ConverterBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<ConverterBlockingStub> {
    private ConverterBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ConverterBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ConverterBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.lab.grpc.converter.ConverterProto.ConvertResponse convert(com.lab.grpc.converter.ConverterProto.ConvertRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getConvertMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service Converter.
   */
  public static final class ConverterFutureStub
      extends io.grpc.stub.AbstractFutureStub<ConverterFutureStub> {
    private ConverterFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ConverterFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ConverterFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.lab.grpc.converter.ConverterProto.ConvertResponse> convert(
        com.lab.grpc.converter.ConverterProto.ConvertRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getConvertMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CONVERT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CONVERT:
          serviceImpl.convert((com.lab.grpc.converter.ConverterProto.ConvertRequest) request,
              (io.grpc.stub.StreamObserver<com.lab.grpc.converter.ConverterProto.ConvertResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getConvertMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.lab.grpc.converter.ConverterProto.ConvertRequest,
              com.lab.grpc.converter.ConverterProto.ConvertResponse>(
                service, METHODID_CONVERT)))
        .build();
  }

  private static abstract class ConverterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ConverterBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.lab.grpc.converter.ConverterProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Converter");
    }
  }

  private static final class ConverterFileDescriptorSupplier
      extends ConverterBaseDescriptorSupplier {
    ConverterFileDescriptorSupplier() {}
  }

  private static final class ConverterMethodDescriptorSupplier
      extends ConverterBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    ConverterMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ConverterGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ConverterFileDescriptorSupplier())
              .addMethod(getConvertMethod())
              .build();
        }
      }
    }
    return result;
  }
}
