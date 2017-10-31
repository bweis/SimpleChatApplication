package chatapplication;import java.io.IOException;import java.io.ObjectInputStream;import java.io.ObjectOutputStream;import java.net.ServerSocket;import java.net.Socket;import java.text.SimpleDateFormat;import java.util.ArrayList;import java.util.Date;import java.util.List;final class ChatServer {    private final List<ClientThread> clients = new ArrayList<>();    private final SimpleDateFormat sdf;    private final int port;    private final ChatFilter chatFilter;    private ChatServer(int port, String badWordsFileName) {        this.port = port;        sdf = new SimpleDateFormat("HH:mm:ss");        chatFilter = new ChatFilter(badWordsFileName);    }    private void start() {        try {            ServerSocket serverSocket = new ServerSocket(port);            while (true) {                display("Server waiting for Clients on port " + port + ".");                Socket socket = serverSocket.accept();                // Get username initially sent                ObjectInputStream sInput = new ObjectInputStream(socket.getInputStream());                String username = "";                try {                    username = (String) sInput.readObject();                } catch (ClassNotFoundException e) {                    e.printStackTrace();                }                ClientThread clientThread = new ClientThread(socket, sInput, username);                Runnable r = clientThread;                Thread t = new Thread(r);                // Check username doesn't exist                if (!usernameExists(username)) {                    clients.add((ClientThread) r);                    t.start();                } else {                    clientThread.writeMsg("> Sorry, a user with username: " + username + " already exists.\n");                    clientThread.close();                }            }        } catch (IOException e) {            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";            display(msg);        }    }    private void display(String msg) {        String time = sdf.format(new Date()) + " " + msg;        System.out.println(time);    }    private synchronized void broadcast(String message) {        String time = sdf.format(new Date());        String formattedMessage = time + " " + chatFilter.filter(message) + "\n";        System.out.print(formattedMessage);        for (ClientThread clientThread : clients) {            if (!clientThread.writeMsg(formattedMessage)) {                clients.remove(clients.indexOf(clientThread));                display("Disconnected Client " + clientThread.username + " removed from list.");            }        }    }    private synchronized void directMessage(String message, String username) {        String time = sdf.format(new Date());        String formattedMessage = time + " " + chatFilter.filter(message) + "\n";        System.out.print(formattedMessage);        for (ClientThread clientThread : clients) {            if (clientThread.username.equalsIgnoreCase(username)) {                clientThread.writeMsg(formattedMessage);            }        }    }    private synchronized void remove(String username) {        for (int i = 0; i < clients.size(); ++i) {            if (clients.get(i).username.equalsIgnoreCase(username)) {                clients.remove(i);                return;            }        }    }    private boolean usernameExists(String username) {        for (ClientThread clientThread : clients) {            if (clientThread.username.equalsIgnoreCase(username)) {                return true;            }        }        return false;    }    /*     *  > java ChatServer     *  > java ChatServer portNumber     *  > java ChatServer portNumber bannedWords.txt     *  If the port number is not specified 1500 is used     */    public static void main(String[] args) {        int portNumber = 1500;        String badWordsFileName = null;        switch (args.length) {            case 2:                badWordsFileName = args[1];                System.out.println("Banned Words File: " + badWordsFileName);            case 1:                try {                    portNumber = Integer.parseInt(args[0]);                } catch (Exception e) {                    System.out.println("Invalid port number.");                    System.out.println("Usage is: > java Server [portNumber]");                    return;                }            case 0:                break;            default:                System.out.println("Usage is: > java Server [portNumber]");                return;        }        ChatServer server;        server = new ChatServer(portNumber, badWordsFileName);        server.start();    }    private final class ClientThread implements Runnable {        Socket socket;        ObjectInputStream sInput;        ObjectOutputStream sOutput;        String username;        ChatMessage cm;        String date;        private ClientThread(Socket socket, ObjectInputStream sInput, String username) {            this.socket = socket;            try {                sOutput = new ObjectOutputStream(socket.getOutputStream());                this.sInput = sInput;                this.username = username;                display(username + " just connected.");            } catch (IOException e) {                display("Exception creating new Input/output Streams: " + e);                close();                return;            }            date = new Date().toString() + "\n";        }        @Override        public void run() {            boolean isRunning = true;            while (isRunning) {                try {                    cm = (ChatMessage) sInput.readObject();                } catch (Exception e) {                    display(username + " Exception reading Streams: " + e);                    break;                }                String message = cm.getMessage();                // Switch on the type of message receive                switch (cm.getType()) {                    case ChatMessage.MESSAGE:                        broadcast(username + ": " + message);                        break;                    case ChatMessage.DM:                        String directMessage = username + " -> " + cm.getRecipient() + ": " + cm.getMessage();                        display(directMessage);                        directMessage(directMessage, username);                        directMessage(directMessage, cm.getRecipient());                        break;                    case ChatMessage.LOGOUT:                        display(username + " disconnected with a LOGOUT message.");                        isRunning = false;                        break;                }            }            remove(username);            close();        }        private void close() {            try {                if (sOutput != null) {                    sOutput.close();                }            } catch (IOException e) {                e.printStackTrace();            }            try {                if (sInput != null) {                    sInput.close();                }            } catch (Exception e) {                e.printStackTrace();            }            try {                if (socket != null) {                    socket.close();                }            } catch (Exception e) {                e.printStackTrace();            }        }        private boolean writeMsg(String msg) {            if (!socket.isConnected()) {                close();                return false;            }            try {                sOutput.writeObject(msg);            } catch (IOException e) {                display("Error sending message to " + username);                display(e.toString());            }            return true;        }    }}