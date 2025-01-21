package com.csc3202.lab.project;

import java.io.*;
import java.net.*;

public class ChatClientSocket {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ChatClientSocket(String serverAddress, int serverPort, String username) throws IOException {
        this.socket = new Socket(serverAddress, serverPort);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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
}
