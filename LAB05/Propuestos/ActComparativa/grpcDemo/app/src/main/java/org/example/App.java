package org.example;

public class App {

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "server".equalsIgnoreCase(args[0])) {
            GrpcBenchmarkServer.main(args);
            return;
        }

        GrpcBenchmarkClient.main(args);
    }
}