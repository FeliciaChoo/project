package com.csc3202.lab.project;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private Set<ClientHandler> clients;

    public ClientHandler(Socket socket, Set<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Get the client's username
            username = in.readLine(); // Assume the first message from the client is the username
            System.out.println("User connected: " + username);
            broadcastMessage("User " + username + " has joined the chat!");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(username + ": " + message);
                broadcastMessage(username + ": " + message);
            }
        } catch (IOException e) {
            System.err.println("Client communication error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void broadcastMessage(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != this) { // Do not send the message back to the sender
                    client.out.println(message);
                }
            }
        }
    }

    private void disconnect() {
        try {
            if (socket != null) socket.close();
            if (out != null) out.close();
            if (in != null) in.close();
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        } finally {
            synchronized (clients) {
                clients.remove(this);
            }
            broadcastMessage("User " + username + " has left the chat.");
            System.out.println("Client disconnected: " + username);
        }
    }
}
