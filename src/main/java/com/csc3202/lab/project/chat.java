package com.csc3202.lab.project;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Popup;

import java.io.File;

public class Chat {
    private ChatClientSocket clientSocket;
    private VBox chatArea;
    private TextField messageInput;
    private String username;
    private String friendUsername;
    private String friendImagePath;
    private VBox root;
    private boolean isGroupChat; // Flag to identify if it's a group chat or private chat

    public Chat(String username, String friendUsername, String friendImagePath, String serverAddress, int serverPort, boolean isGroupChat) {
        this.username = username;
        this.friendUsername = friendUsername;
        this.friendImagePath = friendImagePath;
        this.isGroupChat = isGroupChat;
    
        // Generate a unique room ID for private chats
        String roomId = isGroupChat ? "group_chat_room" : generateRoomId(username, friendUsername);
    
        try {
            this.clientSocket = new ChatClientSocket(serverAddress, serverPort, username, roomId);
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    
        initializeUI();
        receiveMessages();
    }
    
    // Helper method to generate a unique room ID
    private String generateRoomId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
    


    private void initializeUI() {
        root = new VBox();
        root.setStyle("-fx-background-color: #FFB6C1;");
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #FFE4E1;");

        Circle avatar = loadAvatar(friendImagePath);
        Text friendLabel = new Text(friendUsername);
        friendLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        topBar.getChildren().addAll(avatar, friendLabel);

        chatArea = new VBox();
        chatArea.setSpacing(10);
        chatArea.setPadding(new Insets(10));
        ScrollPane chatScrollPane = new ScrollPane(chatArea);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setPrefHeight(400);
        chatScrollPane.setStyle("-fx-background-color: transparent;");

        HBox inputArea = new HBox();
        inputArea.setSpacing(10);
        inputArea.setPadding(new Insets(10));
        inputArea.setAlignment(Pos.CENTER);

        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setPrefWidth(250);
        messageInput.setPrefHeight(40);

        Button emojiButton = new Button("😊");
        emojiButton.setPrefSize(40, 40);
        emojiButton.setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-border-color: #FF69B4; " +
                        "-fx-border-radius: 20; " +
                        "-fx-font-size: 16px;" +
                        "-fx-alignment: center; " +
                        "-fx-min-width: 40px; " +
                        "-fx-min-height: 40px;"
        );
        Popup emojiPicker = createEmojiPicker();
        emojiButton.setOnAction(event -> emojiPicker.show(root.getScene().getWindow()));

        Button cameraButton = new Button("📷");
        cameraButton.setPrefSize(40, 40);
        cameraButton.setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-border-color: #FF69B4; " +
                        "-fx-border-radius: 20; " +
                        "-fx-font-size: 16px;" +
                        "-fx-alignment: center; " +
                        "-fx-min-width: 40px; " +
                        "-fx-min-height: 40px;"
        );
        cameraButton.setOnAction(event -> handleFileSelection());

        Button sendButton = new Button("Send");
        sendButton.setPrefHeight(40);
        sendButton.setStyle(
                "-fx-background-color: #FF69B4; " +
                        "-fx-text-fill: white; " +
                        "-fx-border-radius: 5; " +
                        "-fx-font-size: 14px;" +
                        "-fx-min-height: 40px; " +
                        "-fx-pref-width: 60px; "
        );
        sendButton.setOnAction(event -> sendMessage());

        inputArea.getChildren().addAll(emojiButton, messageInput, cameraButton, sendButton);

        root.getChildren().addAll(topBar, chatScrollPane, inputArea);
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && clientSocket != null) {
            // Format the message with the username
            String formattedMessage = username + ": " + message;
    
            clientSocket.sendMessage(formattedMessage);
    
            // Add the message as a green bubble (sender's message only on the right side)
            addMessage(message, true, false);
    
            messageInput.clear();
        }
    }
    
    private void receiveMessages() {
        if (clientSocket != null) {
            new Thread(() -> {
                try {
                    String receivedMessage;
                    while ((receivedMessage = clientSocket.receiveMessage()) != null) {
                        String finalMessage = receivedMessage.trim();
    
                        // Determine if the message is a system message
                        boolean isSystemMessage = finalMessage.toLowerCase().contains("joined the chat");
    
                        // Parse the sender's username from the received message
                        String sender = isSystemMessage ? "" : finalMessage.split(":")[0].trim();
    
                        // Display the message
                        if (isSystemMessage || !sender.equals(username)) {
                            Platform.runLater(() -> addMessage(finalMessage, false, isSystemMessage));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error receiving messages: " + e.getMessage());
                }
            }).start();
        }
    }
    
    private void addMessage(String message, boolean isUser, boolean isSystemMessage) {
        HBox messageBox = new HBox();
        messageBox.setSpacing(10);
    
        if (isSystemMessage) {
            messageBox.setAlignment(Pos.CENTER);
    
            Text systemMessage = new Text(message);
            systemMessage.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: gray;");
    
            messageBox.getChildren().add(systemMessage);
        } else {
            messageBox.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
    
            TextFlow messageBubble = new TextFlow(new Text(message));
            messageBubble.setPadding(new Insets(10));
            messageBubble.setMaxWidth(250);
            messageBubble.setStyle(isUser
                    ? "-fx-background-color: #DFF0D8; -fx-background-radius: 5;"
                    : "-fx-background-color: #F2DEDE; -fx-background-radius: 5;");
    
            messageBox.getChildren().add(messageBubble);
        }
    
        Platform.runLater(() -> chatArea.getChildren().add(messageBox));
    }
    

    private Popup createEmojiPicker() {
        Popup popup = new Popup();
        GridPane emojiGrid = new GridPane();
        emojiGrid.setPadding(new Insets(10));
        emojiGrid.setHgap(5);
        emojiGrid.setVgap(5);
        String[] emojis = {"😊", "😂", "❤️", "👍", "🎉", "😢", "😡", "🤔", "🙌", "😎"};
        for (int i = 0; i < emojis.length; i++) {
            Button emojiButton = new Button(emojis[i]);
            emojiButton.setStyle("-fx-font-size: 16px;");
            emojiButton.setOnAction(event -> {
                messageInput.setText(messageInput.getText() + emojiButton.getText());
                popup.hide();
            });
            emojiGrid.add(emojiButton, i % 5, i / 5);
        }
        popup.getContent().add(emojiGrid);
        return popup;
    }

    private void handleFileSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            String message = username + " shared an image: " + file.getName();
            clientSocket.sendMessage(message);
            addMessage(message, true,false);
        }
    }

    private Circle loadAvatar(String imagePath) {
        Circle circle = new Circle(15);
        if (imagePath != null && !imagePath.isEmpty()) {
            circle.setFill(new ImagePattern(new Image("file:" + imagePath)));
        } else {
            circle.setFill(Color.LIGHTPINK);
        }
        return circle;
    }

    public VBox getRoot() {
        return root;
    }
}
