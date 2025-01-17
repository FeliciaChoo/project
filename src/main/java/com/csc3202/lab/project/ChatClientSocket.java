package com.csc3202.lab.project;

import java.io.*;
import java.net.*;

public class ChatClientSocket {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public ChatClientSocket(String serverAddress, int serverPort) {
        try {
            // Connect to the server
            socket = new Socket(serverAddress, serverPort);

            // Set up input and output streams
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to the server at " + serverAddress + ":" + serverPort);

        } catch (IOException e) {
            System.err.println("Failed to connect to the server: " + e.getMessage());
        }
    }

    // Send a message to the server
    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    // Receive a message from the server
    public String receiveMessage() {
        try {
            if (input != null) {
                return input.readLine();
            }
        } catch (IOException e) {
            System.err.println("Error reading message: " + e.getMessage());
        }
        return null;
    }

    // Close the socket and streams
    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            System.out.println("Connection closed.");
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Example usage
        ChatClientSocket client = new ChatClientSocket("localhost", 12345);

        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
        try {
            String userInput;
            System.out.println("Type your messages (type 'exit' to quit):");
            while ((userInput = consoleInput.readLine()) != null) {
                if ("exit".equalsIgnoreCase(userInput)) {
                    client.close();
                    break;
                }
                client.sendMessage(userInput);

                String serverReply = client.receiveMessage();
                if (serverReply != null) {
                    System.out.println("Server: " + serverReply);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading user input: " + e.getMessage());
        }
    }
}
