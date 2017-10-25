package chatapplication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final SimpleDateFormat sdf;
    private final int port;

    private ChatServer(int port) {
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss");

    }

    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                display("Server waiting for Clients on port " + port + ".");
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        System.out.println(time);
    }

    private synchronized void broadcast(String message) {
        String time = sdf.format(new Date());
        String formattedMessage = time + " " + message + "\n";
        System.out.print(formattedMessage);

        for (ClientThread clientThread : clients) {
            if (!clientThread.writeMsg(formattedMessage)) {
                clients.remove(clients.indexOf(clientThread));
                display("Disconnected Client " + clientThread.username + " removed from list.");
            }
        }
    }

    private synchronized void remove(int id) {
        for (int i = 0; i < clients.size(); ++i) {
            if (clients.get(i).id == id) {
                clients.remove(i);
                return;
            }
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        int portNumber = 1500;
        switch (args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;

        }

        ChatServer server = new ChatServer(portNumber);
        server.start();
    }

    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;
        String date;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                display(username + " just connected.");
            } catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                close();
                return;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            }
            date = new Date().toString() + "\n";
        }

        @Override
        public void run() {
            boolean isRunning = true;
            while (isRunning) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (Exception e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                }

                String message = cm.getMessage();

                // Switch on the type of message receive
                switch (cm.getType()) {
                    case ChatMessage.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case ChatMessage.LOGOUT:
                        display(username + " disconnected with a LOGOUT message.");
                        isRunning = false;
                        break;
                }
            }
            remove(id);
            close();
        }

        private void close() {
            try {
                if (sOutput != null) {
                    sOutput.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (sInput != null) {
                    sInput.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean writeMsg(String msg) {
            if (!socket.isConnected()) {
                close();
                return false;
            }
            try {
                sOutput.writeObject(msg);
            } catch (IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
    }
}
