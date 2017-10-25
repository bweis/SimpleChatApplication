package chatapplication;

import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    static final int MESSAGE = 0, LOGOUT = 1;
    private int type;
    private String message;

    ChatMessage(int type, String message) {
        this.message = message;
        if (this.message == null) {
            this.message = "";
        }
        this.type = type;
    }

    int getType() {
        return type;
    }

    String getMessage() {
        return message;
    }
}
// Test
