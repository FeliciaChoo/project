package com.csc3202.lab.project;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServerSocket {
    private static final int PORT = 12345;

    // Map to hold chat rooms, where the key is the roomId and value is the set of clients in that room
    private static final Map<String, Set<ClientHandler>> chatRooms = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        System.out.println("Chat server is running on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Accept incoming client connections
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a new client handler thread for this connection
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Add a client to a chat room
    public static void addToRoom(String roomId, ClientHandler client) {
        chatRooms.putIfAbsent(roomId, Collections.synchronizedSet(new HashSet<>()));
        chatRooms.get(roomId).add(client);
    }

    // Remove a client from a chat room
    public static void removeFromRoom(String roomId, ClientHandler client) {
        Set<ClientHandler> room = chatRooms.get(roomId);
        if (room != null) {
            room.remove(client);
            if (room.isEmpty()) {
                chatRooms.remove(roomId); // Remove the room if it's empty
            }
        }
    }

    // Broadcast a message to all clients in a specific room
    public static void broadcastMessage(String roomId, String message) {
        Set<ClientHandler> room = chatRooms.get(roomId);
        if (room != null) {
            synchronized (room) {
                for (ClientHandler client : room) {
                    client.sendMessage(message);
                }
            }
        }
    }
}
