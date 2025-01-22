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

    public void sendMessage(String message) {
        out.println(message);
    }

    public String receiveMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Error reading message: " + e.getMessage());
        }
        return null;
    }

    public void disconnect(String username) {
        try {
            sendMessage("User " + username + " has left the chat.");
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }


public void sendImage(byte[] imageBytes) {
    try {
        String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
        sendMessage("IMAGE:" + encodedImage);
    } catch (Exception e) {
        e.printStackTrace();
    }
}


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
