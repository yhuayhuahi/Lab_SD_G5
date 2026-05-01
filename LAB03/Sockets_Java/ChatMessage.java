// ===============================
// ChatMessage.java
// ===============================

import java.io.Serializable;

/*
 * This class defines the different types of messages
 * exchanged between Clients and Server.
 */
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    // Message types
    static final int WHOISIN = 0;
    static final int MESSAGE = 1;
    static final int LOGOUT = 2;

    private int type;
    private String message;

    // Constructor
    ChatMessage(int type, String message) {
        this.type = type;
        this.message = message;
    }

    int getType() {
        return type;
    }

    String getMessage() {
        return message;
    }
}