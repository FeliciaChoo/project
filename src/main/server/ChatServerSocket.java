package com.csc3202.lab.project.server;

import com.csc3202.lab.project.common.chatMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChatServerSocket {
    private static final int PORT = 5000;
    private static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Chat Server is running...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // 广播消息
    static void broadcastMessage(chatMessage message) {
        clients.values().forEach(client -> client.sendMessage(message));
    }

    // 添加客户端
    static void addClient(String username, ClientHandler clientHandler) {
        clients.put(username, clientHandler);
        updateAllUsers();
    }

    // 移除客户端
    static void removeClient(String username) {
        clients.remove(username);
        updateAllUsers();
    }

    // 更新用户列表
    static void updateAllUsers() {
        List<String> userList = clients.keySet().stream().sorted().collect(Collectors.toList());
        clients.values().forEach(client -> client.sendUserList(userList));
    }
}
