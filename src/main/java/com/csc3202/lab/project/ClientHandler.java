package com.csc3202.lab.project;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
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

            String message;
            while ((message = in.readLine()) != null) {
                broadcastMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.out.println(message);
        }
    }
}
