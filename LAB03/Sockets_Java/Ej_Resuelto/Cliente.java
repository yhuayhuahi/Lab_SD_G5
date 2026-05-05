import java.io.*;
import java.net.*;
public class Cliente { 

    static final String HOST = "localhost";  
    static final int PUERTO = 5000; 

    public Cliente() {
        try {
            Socket skCliente = new Socket(HOST, PUERTO); 
            InputStream aux = skCliente.getInputStream(); 
            DataInputStream flujo = new DataInputStream(aux); 
            System.out.println(flujo.readUTF()); // Leer mensaje del servidor
            skCliente.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Cliente();
    }
}