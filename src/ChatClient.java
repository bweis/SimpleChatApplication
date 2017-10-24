import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private String server, username;
    private int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    private boolean start() {
        try {
            socket = new Socket(server, port);
        } catch (Exception e) {
            display("Exception connecting to socket: " + e);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        try {
            sOutput.writeObject(username);
        } catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        return true;
    }

    private void display(String msg) {
        System.out.println(msg);
    }

    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            display("Exception writing to server: " + e);
        }
    }

    private void disconnect() {
        try {
            if (sInput != null) sInput.close();
        } catch (Exception ignored) {
        }
        try {
            if (sOutput != null) sOutput.close();
        } catch (Exception ignored) {
        }
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {
        }
    }

    public static void main(String[] args) {
        int portNumber = 1500;
        String serverAddress = "localhost";
        String userName = "Anonymous";

        switch (args.length) {
            case 3:
                serverAddress = args[2];
            case 2:
                try {
                    portNumber = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                    return;
                }
            case 1:
                userName = args[0];
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
                return;
        }
        ChatClient client = new ChatClient(serverAddress, portNumber, userName);
        if (!client.start())
            return;

        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String msg = scan.nextLine();
            if (msg.equalsIgnoreCase("LOGOUT")) {
                client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
                break;
            } else {
                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
            }
        }
        client.disconnect();
    }

    class ListenFromServer implements Runnable {
        public void run() {
            while (true) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                    System.out.print("> ");
                } catch (IOException e) {
                    display("Server has closed the connection");
                    break;
                }
                catch (ClassNotFoundException ignored) { }
            }
        }
    }
}
