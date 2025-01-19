
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClientSocket {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ChatClientSocket(String serverAddress, int serverPort, String username) throws IOException {
        this.socket = new Socket(serverAddress, serverPort);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.username = username;

        // Notify the server that the client is online
        sendMessage("User " + username + " has connected to the server.");
    }

    public void sendMessage(String message) {
        out.println(username + ": " + message);
    }

    public void receiveMessages() {
        // Start a thread to handle server messages
        Thread messageReceiver = new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println(serverMessage); // Print messages broadcast by the server
                }
            } catch (IOException e) {
                System.err.println("Failed to read messages from the server: " + e.getMessage());
            }
        });
        messageReceiver.start();
    }

    public void disconnect() {
        try {
            sendMessage("User " + username + " has disconnected.");
            socket.close();
        } catch (IOException e) {
            System.err.println("Unable to disconnect: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Get server address and username from the command line
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the server address: ");
        String serverAddress = scanner.nextLine().trim();

        System.out.print("Enter your username: ");
        String username = scanner.nextLine().trim();

        try {
            ChatClientSocket client = new ChatClientSocket(serverAddress, 12345, username);
            client.receiveMessages();

            System.out.println("Connected successfully. Type a message and press Enter to send (type 'exit' to disconnect):");

            while (true) {
                String message = scanner.nextLine();
                if ("exit".equalsIgnoreCase(message)) {
                    client.disconnect();
                    System.out.println("Disconnected.");
                    break;
                }
                client.sendMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Unable to connect to the server: " + e.getMessage());
        }
    }
}


