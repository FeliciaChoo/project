package com.csc3202.lab.project;

import java.io.*;
import java.net.*;

public class ChatClientSocket {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ChatClientSocket(String serverAddress, int port, String username) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.println(username);  // Send the username to the server
    }

    // Send message to the server
    public void sendMessage(String message) {
        out.println(message);
    }

    // Receive message from the server
    public String receiveMessage() throws IOException {
        return in.readLine();  // Read a line of message from the server
    }

    // Close the socket
    public void close() throws IOException {
        socket.close();
    }
}
