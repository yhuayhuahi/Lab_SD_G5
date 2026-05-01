// ===============================
// Client.java
// ===============================

import java.net.*;
import java.io.*;
import java.util.*;

public class Client {

    // Notification string
    private final String notif = " *** ";

    // Streams
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;

    // Socket
    private Socket socket;

    // Server info
    private String server;
    private String username;
    private int port;

    // Constructor
    Client(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * Start the client
     */
    public boolean start() {

        // Connect to server
        try {
            socket = new Socket(server, port);
        } catch (Exception e) {
            display("Error connecting to server: " + e.getMessage());
            return false;
        }

        String msg = "Connection accepted " +
                socket.getInetAddress() + ":" + socket.getPort();

        display(msg);

        // Create streams
        try {

            // IMPORTANT:
            // Create ObjectOutputStream FIRST
            sOutput = new ObjectOutputStream(socket.getOutputStream());
            sOutput.flush();

            sInput = new ObjectInputStream(socket.getInputStream());

        } catch (IOException eIO) {
            display("Exception creating streams: " + eIO.getMessage());
            return false;
        }

        // Create listener thread
        new ListenFromServer().start();

        // Send username
        try {
            sOutput.writeObject(username);
            sOutput.flush();
        } catch (IOException eIO) {
            display("Exception during login: " + eIO.getMessage());
            disconnect();
            return false;
        }

        return true;
    }

    /*
     * Display message
     */
    private void display(String msg) {
        System.out.println(msg);
    }

    /*
     * Send message to server
     */
    void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
            sOutput.flush();
        } catch (IOException e) {
            display("Exception writing to server: " + e.getMessage());
        }
    }

    /*
     * Disconnect client
     */
    private void disconnect() {

        try {
            if (sInput != null)
                sInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (sOutput != null)
                sOutput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (socket != null)
                socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Main method
     */
    public static void main(String[] args) {

        int portNumber = 1500;
        String serverAddress = "localhost";
        String userName = "Anonymous";

        Scanner scan = new Scanner(System.in);

        System.out.print("Enter username: ");
        userName = scan.nextLine();

        switch (args.length) {

            case 3:
                serverAddress = args[2];

            case 2:
                try {
                    portNumber = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage: java Client [username] [portNumber] [serverAddress]");
                    scan.close();
                    return;
                }

            case 1:
                userName = args[0];

            case 0:
                break;

            default:
                System.out.println("Usage: java Client [username] [portNumber] [serverAddress]");
                scan.close();
                return;
        }

        // Create client
        Client client = new Client(serverAddress, portNumber, userName);

        // Start client
        if (!client.start()) {
            scan.close();
            return;
        }

        System.out.println("\nHello! Welcome to the chatroom.");
        System.out.println("Instructions:");
        System.out.println("1. Type message to broadcast.");
        System.out.println("2. Type '@username message' for private message.");
        System.out.println("3. Type 'WHOISIN' to see active users.");
        System.out.println("4. Type 'LOGOUT' to exit.");

        // Main loop
        while (true) {

            System.out.print("> ");

            String msg = scan.nextLine();

            if (msg.equalsIgnoreCase("LOGOUT")) {

                client.sendMessage(
                        new ChatMessage(ChatMessage.LOGOUT, "")
                );

                break;

            } else if (msg.equalsIgnoreCase("WHOISIN")) {

                client.sendMessage(
                        new ChatMessage(ChatMessage.WHOISIN, "")
                );

            } else {

                client.sendMessage(
                        new ChatMessage(ChatMessage.MESSAGE, msg)
                );
            }
        }

        scan.close();
        client.disconnect();
    }

    /*
     * Thread to listen from server
     */
    class ListenFromServer extends Thread {

        public void run() {

            while (true) {

                try {

                    String msg = (String) sInput.readObject();

                    System.out.println(msg);
                    System.out.print("> ");

                } catch (IOException e) {

                    display(notif + "Server has closed the connection: " + e.getMessage() + notif);
                    break;

                } catch (ClassNotFoundException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}