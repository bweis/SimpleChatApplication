import java.io.*;

public class ChatMessage implements Serializable {
    protected static final long serialVersionUID = 6898543889087L;

    static final int MESSAGE = 0, LOGOUT = 1;
    private int type;
    private String message;

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