package com.csc3202.lab.project;

import java.io.*;
import java.net.*;
import java.util.Base64;

public class ChatClientSocket {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DataOutputStream outputStream;  // DataOutputStream for sending image data
    private DataInputStream inputStream;    // DataInputStream for receiving image data

    public ChatClientSocket(String serverAddress, int serverPort, String username) throws IOException {
        this.socket = new Socket(serverAddress, serverPort);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Initialize DataOutputStream and DataInputStream for image data
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.inputStream = new DataInputStream(socket.getInputStream());

        sendMessage("User " + username + " has joined the chat.");
    }

    // Modified to handle private messages with the prefix
    public void sendMessage(String message) {
        out.println(message);  // Send the message to the server
    }

    // New method to handle sending a private message
    public void sendPrivateMessage(String friendUsername, String message) {
        String formattedMessage = "PRIVATE:" + friendUsername + ":" + message;
        sendMessage(formattedMessage);  // Send the formatted private message
    }

    // Method to receive messages
    public String receiveMessage() {
        try {
            String receivedMessage = in.readLine();
            if (receivedMessage != null && receivedMessage.startsWith("PRIVATE:")) {
                // If the message is a private message, format it accordingly
                return receivedMessage;  // We will handle this in the UI
            }
            return receivedMessage;  // For general or system messages
        } catch (IOException e) {
            System.err.println("Error reading message: " + e.getMessage());
        }
        return null;
    }

    // Method to disconnect and send the user exit message
    public void disconnect(String username) {
        try {
            sendMessage("User " + username + " has left the chat.");
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    // Method to send image
    public void sendImage(byte[] imageBytes) {
        try {
            String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
            sendMessage("IMAGE:" + encodedImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to receive image
    public byte[] receiveImage() {
        try {
            // Receive the image size
            int length = inputStream.readInt();
            byte[] imageBytes = new byte[length];

            // Read the image data into the byte array
            inputStream.readFully(imageBytes);
            return imageBytes;
        } catch (IOException e) {
            System.err.println("Error receiving image: " + e.getMessage());
            return null;
        }
    }
}
