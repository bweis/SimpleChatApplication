package chatapplication;

import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    protected static final int MESSAGE = 0, LOGOUT = 1;
    private final int type;
    private final String message;

    ChatMessage(int type, String message) {
        if (message == null) {
            this.message = "";
        } else {
            this.message = message;
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
