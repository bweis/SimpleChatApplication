package chatapplication;

import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    static final int MESSAGE = 0, LOGOUT = 1, DM = 2;
    private final int type;
    private final String message;
    private final String recipient;

    ChatMessage(int type, String message, String recipient) {
        if (message == null) {
            this.message = "";
        } else {
            this.message = message;
        }

        if (recipient == null) {
            this.recipient = "";
        } else {
            this.recipient = recipient;
        }

        this.type = type;
    }

    int getType() {
        return type;
    }

    String getMessage() {
        return message;
    }

    String getRecipient() {
        return recipient;
    }
}
