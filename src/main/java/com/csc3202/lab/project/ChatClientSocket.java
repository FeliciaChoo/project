package com.csc3202.lab.project;

import java.io.*;
import java.net.*;

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

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public void disconnect() {
        try {
            sendMessage("User " + username + " has disconnected.");
            socket.close();
        } catch (IOException e) {
            System.err.println("Unable to disconnect: " + e.getMessage());
        }
    }
}
