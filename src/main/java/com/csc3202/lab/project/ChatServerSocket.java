package com.csc3202.lab.project;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServerSocket {
    private static final int PORT = 5000; // Server listening port
    private static Set<ClientHandler> clients = new HashSet<>(); // Active client collection

    public static void main(String[] args) {
        System.out.println("Chat server started, listening on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                ClientHandler handler = new ClientHandler(clientSocket, clients);
                clients.add(handler);
                handler.start(); // Start the client handler thread
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}