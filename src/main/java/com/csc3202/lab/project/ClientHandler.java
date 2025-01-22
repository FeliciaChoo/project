package com.csc3202.lab.project;

import java.io.*;
import java.net.*;
import java.util.*;

class ClientHandler extends Thread {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private String username;
    private String roomId;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            // Read the client's username and roomId from the input
            out.println("Enter your username:");
            username = in.readLine();

            out.println("Enter room ID (or type 'exit' to disconnect):");
            roomId = in.readLine();

            if ("exit".equalsIgnoreCase(roomId)) {
                disconnect();
                return;
            }

            // Add this client to the chat room
            ChatServerSocket.addToRoom(roomId, this);
            ChatServerSocket.broadcastMessage(roomId, username + " has joined the chat.");

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                // Broadcast the message to the room
                ChatServerSocket.broadcastMessage(roomId, username + ": " + message);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private void disconnect() {
        try {
            if (roomId != null) {
                ChatServerSocket.removeFromRoom(roomId, this);
                ChatServerSocket.broadcastMessage(roomId, username + " has left the chat.");
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Error disconnecting client: " + e.getMessage());
        }
    }
}
