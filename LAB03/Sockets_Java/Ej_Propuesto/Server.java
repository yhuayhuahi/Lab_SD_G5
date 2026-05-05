// ===============================
// Server.java
// ===============================

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {

    // Unique ID
    private static int uniqueId;

    // List of clients
    private final List<ClientThread> al;

    // Time format
    private final SimpleDateFormat sdf;

    // Port
    private int port;

    // Running flag
    private boolean keepGoing;

    // Notification
    private final String notif = " *** ";

    /*
     * Constructor
     */
    public Server(int port) {

        this.port = port;

        sdf = new SimpleDateFormat("HH:mm:ss");

        al = Collections.synchronizedList(new ArrayList<>());
    }

    /*
     * Start server
     */
    public void start() {

        keepGoing = true;

        try {

            ServerSocket serverSocket = new ServerSocket(port);

            while (keepGoing) {

                display("Server waiting for Clients on port " + port + ".");

                Socket socket = serverSocket.accept();

                if (!keepGoing)
                    break;

                ClientThread t = new ClientThread(socket);

                al.add(t);

                t.start();
            }

            // Close server
            try {

                serverSocket.close();

                synchronized (al) {

                    for (ClientThread tc : al) {

                        try {
                            tc.sInput.close();
                            tc.sOutput.close();
                            tc.socket.close();
                        } catch (IOException ioE) {
                            ioE.printStackTrace();
                        }
                    }
                }

            } catch (Exception e) {
                display("Exception closing server: " + e.getMessage());
            }

        } catch (IOException e) {

            String msg = sdf.format(new Date()) +
                    " Exception on new ServerSocket: " + e.getMessage();

            display(msg);
        }
    }

    /*
     * Stop server
     */
    protected void stop() {

        keepGoing = false;

        try {
            new Socket("localhost", port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Display message
     */
    private void display(String msg) {

        String time = sdf.format(new Date()) + " " + msg;

        System.out.println(time);
    }

    /*
     * Broadcast message
     */
    private synchronized boolean broadcast(String message) {

        String time = sdf.format(new Date());

        String[] w = message.split(" ", 3);

        boolean isPrivate = false;

        if (w.length > 1 && w[1].startsWith("@")) {
            isPrivate = true;
        }

        // Private message
        if (isPrivate) {

            String toCheck = w[1].substring(1);

            message = w[0] + " " + w[2];

            String messageLf = time + " " + message + "\n";

            boolean found = false;

            synchronized (al) {

                for (int y = al.size() - 1; y >= 0; y--) {

                    ClientThread ct1 = al.get(y);

                    String check = ct1.getUsername();

                    if (check.equals(toCheck)) {

                        if (!ct1.writeMsg(messageLf)) {

                            al.remove(y);

                            display("Disconnected Client " +
                                    ct1.username + " removed from list.");
                        }

                        found = true;
                        break;
                    }
                }
            }

            if (!found)
                return false;
        }

        // Broadcast message
        else {

            String messageLf = time + " " + message + "\n";

            System.out.print(messageLf);

            synchronized (al) {

                for (int i = al.size() - 1; i >= 0; i--) {

                    ClientThread ct = al.get(i);

                    if (!ct.writeMsg(messageLf)) {

                        al.remove(i);

                        display("Disconnected Client " +
                                ct.username + " removed from list.");
                    }
                }
            }
        }

        return true;
    }

    /*
     * Remove client
     */
    synchronized void remove(int id) {

        String disconnectedClient = "";

        synchronized (al) {

            for (int i = 0; i < al.size(); i++) {

                ClientThread ct = al.get(i);

                if (ct.id == id) {

                    disconnectedClient = ct.getUsername();

                    al.remove(i);

                    break;
                }
            }
        }

        broadcast(notif + disconnectedClient + " has left the chat room." + notif);
    }

    /*
     * Main
     */
    public static void main(String[] args) {

        int portNumber = 1500;

        switch (args.length) {

            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage: java Server [portNumber]");
                    return;
                }

            case 0:
                break;

            default:
                System.out.println("Usage: java Server [portNumber]");
                return;
        }

        Server server = new Server(portNumber);

        server.start();
    }

    /*
     * Client Thread
     */
    class ClientThread extends Thread {

        Socket socket;

        ObjectInputStream sInput;
        ObjectOutputStream sOutput;

        int id;

        String username;

        ChatMessage cm;

        String date;

        /*
         * Constructor
         */
        ClientThread(Socket socket) {

            id = ++uniqueId;

            this.socket = socket;

            System.out.println("Thread creating streams...");

            try {

                // IMPORTANT:
                // Output stream FIRST
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sOutput.flush();

                sInput = new ObjectInputStream(socket.getInputStream());

                username = (String) sInput.readObject();

                broadcast(notif + username + " has joined the chat room." + notif);

            } catch (IOException e) {

                display("Exception creating streams: " + e.getMessage());

                return;

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            date = new Date().toString();
        }

        public String getUsername() {
            return username;
        }

        /*
         * Thread execution
         */
        public void run() {

            boolean keepGoing = true;

            while (keepGoing) {

                try {

                    cm = (ChatMessage) sInput.readObject();

                } catch (IOException e) {

                    display(username + " Exception reading streams: " + e.getMessage());

                    break;

                } catch (ClassNotFoundException e2) {

                    e2.printStackTrace();

                    break;
                }

                String message = cm.getMessage();

                switch (cm.getType()) {

                    case ChatMessage.MESSAGE:

                        boolean confirmation =
                                broadcast(username + ": " + message);

                        if (!confirmation) {

                            String msg =
                                    notif + "Sorry. No such user exists." + notif;

                            writeMsg(msg);
                        }

                        break;

                    case ChatMessage.LOGOUT:

                        display(username + " disconnected with a LOGOUT message.");

                        keepGoing = false;

                        break;

                    case ChatMessage.WHOISIN:

                        writeMsg(
                                "List of users connected at " +
                                        sdf.format(new Date()) + "\n"
                        );

                        synchronized (al) {

                            for (int i = 0; i < al.size(); i++) {

                                ClientThread ct = al.get(i);

                                writeMsg(
                                        (i + 1) + ") " +
                                                ct.username +
                                                " since " +
                                                ct.date
                                );
                            }
                        }

                        break;
                }
            }

            remove(id);

            close();
        }

        /*
         * Close everything
         */
        private void close() {

            try {
                if (sOutput != null)
                    sOutput.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (sInput != null)
                    sInput.close();
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
         * Send message to client
         */
        private boolean writeMsg(String msg) {

            if (socket == null || socket.isClosed()) {

                close();

                return false;
            }

            try {

                sOutput.writeObject(msg);

                sOutput.flush();

            } catch (IOException e) {

                display(notif +
                        "Error sending message to " +
                        username +
                        notif);

                display(e.toString());

                return false;
            }

            return true;
        }
    }
}