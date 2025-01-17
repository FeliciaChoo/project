package com.csc3202.lab.project;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ChatClientSocket {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private TextArea chatArea;
    private ListView<String> userList;

    public ChatClientSocket(String host, int port, String username) {
        try {
            this.socket = new Socket(host, port);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            this.username = username;

            // 发送用户名到服务器
            out.writeObject(username);

            // 启动新线程监听服务器消息
            new Thread(this::listenForMessages).start();
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private void listenForMessages() {
        try {
            Object message;
            while ((message = in.readObject()) != null) {
                if (message instanceof chatMessage) {
                    handleChatMessage((chatMessage) message);
                } else if (message instanceof List) {
                    handleUserListUpdate((List<String>) message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Connection closed.");
        }
    }

    private void handleChatMessage(chatMessage message) {
        String formattedMessage = "[" + message.getTimestamp() + "] " + message.getSender() + ": " + message.getContent();
        Platform.runLater(() -> {
            if (chatArea != null) {
                chatArea.appendText(formattedMessage + "\n");
            }
        });
    }

    private void handleUserListUpdate(List<String> users) {
        Platform.runLater(() -> {
            if (userList != null) {
                userList.getItems().clear();
                userList.getItems().addAll(users);
            }
        });
    }

    public void sendMessage(chatMessage message) {
        try {
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setChatArea(TextArea chatArea) {
        this.chatArea = chatArea;
    }

    public void setUserList(ListView<String> userList) {
        this.userList = userList;
    }
}
