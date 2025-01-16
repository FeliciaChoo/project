package com.csc3202.lab.project.server;

import com.csc3202.lab.project.common.chatMessage;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            username = (String) in.readObject();
            ChatServerSocket.addClient(username, this);

            Object message;
            while ((message = in.readObject()) != null) {
                if (message instanceof chatMessage) {
                    ChatServerSocket.broadcastMessage((chatMessage) message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection error for user: " + username);
        } finally {
            ChatServerSocket.removeClient(username);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(chatMessage message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUserList(List<String> userList) {
        try {
            out.writeObject(userList);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
