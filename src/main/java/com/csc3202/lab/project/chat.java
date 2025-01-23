package com.csc3202.lab.project;

import java.io.ByteArrayInputStream;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import java.nio.file.Files;
import java.util.Base64;
import java.io.File;
import java.io.IOException;

public class Chat {
    private ChatClientSocket clientSocket;
    private Main mainApp;
    private VBox chatArea;
    private TextField messageInput;
    private String username;
    private String friendUsername;
    private String friendImagePath;
    private VBox root;
    private boolean isGroupChat;
    private ChatClientSocket chatClientSocket;
    

    public Chat(String username, String friendUsername, String friendImagePath, String serverAddress, int serverPort, boolean isGroupChat, Main mainApp) {
        this.username = username;
        this.friendUsername = friendUsername;
        this.friendImagePath = friendImagePath;
        this.isGroupChat = isGroupChat;
        this.mainApp = mainApp; // Initialize mainApp

        try {
            this.clientSocket = new ChatClientSocket(serverAddress, serverPort, username);
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }

        initializeUI();
        receiveMessages();
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

        Button qcButton = new Button("Quit Chat");
        qcButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-border-radius: 3; -fx-font-size: 14px; -fx-min-width: 60px; -fx-min-height: 40px;");
        qcButton.setOnAction(event -> quitChat());

        HBox.setHgrow(qcButton, Priority.ALWAYS);
        HBox.setMargin(qcButton, new Insets(0, 30, 0, 0));

        topBar.getChildren().addAll(avatar, friendLabel, qcButton);

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
        emojiButton.setStyle("-fx-background-color: #FF69B4; -fx-border-color: #FF69B4; -fx-border-radius: 20; -fx-font-size: 16px; -fx-alignment: center; -fx-min-width: 40px; -fx-min-height: 40px;");

        // Integrating the emoji picker with the style from your earlier design
        Popup emojiPicker = createEmojiPicker();
        emojiButton.setOnAction(event -> emojiPicker.show(root.getScene().getWindow()));

        Button cameraButton = new Button("📷");
        cameraButton.setPrefSize(40, 40);
        cameraButton.setStyle("-fx-background-color: #FF69B4; -fx-border-color: #FF69B4; -fx-border-radius: 20; -fx-font-size: 16px; -fx-alignment: center; -fx-min-width: 40px; -fx-min-height: 40px;");
        cameraButton.setOnAction(event -> handleFileSelection());

        Button emojiSendButton = new Button("▶");
        emojiSendButton.setPrefHeight(40);
        emojiSendButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-border-radius: 5; -fx-font-size: 14px; -fx-min-height: 40px; -fx-pref-width: 60px;");
        emojiSendButton.setOnAction(event -> sendMessage());

        inputArea.getChildren().addAll(emojiButton, messageInput, cameraButton, emojiSendButton);

        root.getChildren().addAll(topBar, chatScrollPane, inputArea);
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && clientSocket != null) {
            if (isGroupChat) {
                String formattedMessage = username + ": " + message;
                clientSocket.sendMessage(formattedMessage);
                addMessage(message, true, false); // Show on the right (sent message)
            } else {
                String formattedMessage = "PRIVATE:" + username + ":" + message;
                clientSocket.sendPrivateMessage(friendUsername, formattedMessage);
                addMessage(message, true, false); // Show on the right (sent message)
            }
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
    
                        if (finalMessage.startsWith("PRIVATE:")) {
                            String[] parts = finalMessage.split(":", 3);
                            String sender = parts[1];  
                            String privateMessage = parts[2];  
    
                            if (sender.equals(username) || sender.equals(friendUsername)) {
                                boolean isUserMessage = sender.equals(username);
                                Platform.runLater(() -> addMessage(privateMessage, !isUserMessage, false));
                            }
                        } else if (finalMessage.startsWith("IMAGE:")) {
                            String base64Image = finalMessage.substring(6); 
                            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                            Platform.runLater(() -> displayImage(imageBytes));
                        } else {
                            boolean isSystemMessage = finalMessage.toLowerCase().contains("joined the chat") || 
                                              finalMessage.toLowerCase().contains("left the chat");
                            String sender = isSystemMessage ? "" : finalMessage.split(":")[0].trim();
    
                            if (isSystemMessage || !sender.equals(username)) {
                                Platform.runLater(() -> addMessage(finalMessage, false, isSystemMessage));
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error receiving messages: " + e.getMessage());
                }
            }).start();
        }
    }
    
    
    private Popup createEmojiPicker() {
        Popup popup = new Popup();
        GridPane emojiGrid = new GridPane();
        emojiGrid.setPadding(new Insets(10));
        emojiGrid.setHgap(5);
        emojiGrid.setVgap(5);
        emojiGrid.setStyle("-fx-background-color: #FFC0CB; -fx-border-color: #FF69B4; -fx-border-width: 2px; -fx-border-radius: 5px;");

        String[] emojis = {"😊", "😂", "❤️", "👍", "🎉", "😢", "😡", "🤔", "🙌", "😎"};
        for (int i = 0; i < emojis.length; i++) {
            Button emojiChoice = new Button(emojis[i]);
            emojiChoice.setStyle("-fx-font-size: 16px; -fx-background-color: transparent;");
            emojiChoice.setOnAction(event -> {
                messageInput.setText(messageInput.getText() + emojiChoice.getText());
                popup.hide();
            });
            emojiGrid.add(emojiChoice, i % 5, i / 5);
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
            try {
                byte[] imageBytes = Files.readAllBytes(file.toPath());
                clientSocket.sendImage(imageBytes);
                addMessage("You shared an image", true, false);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to read image file: " + e.getMessage());
            }
        }
    }

    private void displayImage(byte[] imageBytes) {
        Image image = new Image(new ByteArrayInputStream(imageBytes));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);

        HBox imageBox = new HBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.getChildren().add(imageView);

        Platform.runLater(() -> chatArea.getChildren().add(imageBox));
    }
    
    private void quitChat() {
        if (clientSocket != null) {
            clientSocket.disconnect(username);
        }
        if (mainApp != null) {
            mainApp.loadMainScreen(username);
        } else {
            System.err.println("Main app reference is null.");
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
    
            if (message.startsWith("PRIVATE:")) {
                messageBubble.setStyle("-fx-background-color: #FFF8DC; -fx-border-color: #FFA07A; -fx-border-width: 1; -fx-background-radius: 5;");
            }
    
            messageBox.getChildren().add(messageBubble);
        }
    
        Platform.runLater(() -> chatArea.getChildren().add(messageBox));
    }
}
