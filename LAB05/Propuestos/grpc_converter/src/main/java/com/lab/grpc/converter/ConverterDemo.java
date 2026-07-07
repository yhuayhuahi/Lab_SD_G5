package com.lab.grpc.converter;

public class ConverterDemo {

    public static void main(String[] args) throws Exception {
        ConverterClient client = new ConverterClient("localhost", 50051);

        try {
            client.printCatalog();

            client.convert("longitud", "m", "km", 1500);
            client.convert("masa", "kg", "g", 2.5);
            client.convert("area", "ha", "m2", 3);
            client.convert("volumen", "l", "m3", 1000);
            client.convert("temperatura", "C", "F", 25);
            client.convert("moneda", "PEN", "USD", 370);
            client.convert("tiempo", "h", "min", 2);
            client.convert("velocidad", "km_h", "m_s", 90);
            client.convert("densidad", "g_cm3", "kg_m3", 1);
            client.convert("caudal", "m3_h", "l_s", 36);
        } finally {
            client.shutdown();
        }
    }
}