package com.csc3202.lab.project;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

import java.io.File;

public class Chat extends Application {
    private ChatClientSocket clientSocket;
    private VBox chatArea;
    private TextField messageInput;
    private String username;
    private String friendUsername;
    private String friendImagePath;

    // "Temporary no-argument constructor for testing purposes".
    public Chat() {
        // "Initialize with default values".
        this("Mike", "Mike", null, "127.0.0.1", 12345);
    }

    public Chat(String username, String friendUsername, String friendImagePath, String serverAddress, int serverPort) {
        this.username = username;
        this.friendUsername = friendUsername;
        this.friendImagePath = friendImagePath;

        try {
            this.clientSocket = new ChatClientSocket(serverAddress, serverPort, username);
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) {
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #FFB6C1;");
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        // 顶部好友信息栏
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #FFE4E1;");

        Circle avatar = loadAvatar(friendImagePath);
        Text friendLabel = new Text(friendUsername);
        friendLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        topBar.getChildren().addAll(avatar, friendLabel);

        // 聊天内容区
        chatArea = new VBox();
        chatArea.setSpacing(10);
        chatArea.setPadding(new Insets(10));
        ScrollPane chatScrollPane = new ScrollPane(chatArea);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setPrefHeight(400);
        chatScrollPane.setStyle("-fx-background-color: transparent;");

        // 输入区域
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
        emojiButton.setOnAction(event -> emojiPicker.show(stage));

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

        // 将控件添加到输入区域
        inputArea.getChildren().addAll(emojiButton, messageInput, cameraButton, sendButton);

        root.getChildren().addAll(topBar, chatScrollPane, inputArea);

        Scene scene = new Scene(root, 400, 600);
        stage.setMinWidth(400);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.setTitle("HEART2HEART - " + username);
        stage.show();

        receiveMessages();
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && clientSocket != null) {
            clientSocket.sendMessage(message);
            addMessage(username + ": " + message, true);
            messageInput.clear();
        }
    }

    private void receiveMessages() {
        if (clientSocket != null) {
            new Thread(() -> {
                try {
                    while (true) {
                        String receivedMessage = clientSocket.receiveMessage();
                        if (receivedMessage != null) {
                            addMessage(receivedMessage, false);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error receiving messages: " + e.getMessage());
                }
            }).start();
        }
    }

    private void addMessage(String message, boolean isUser) {
        HBox messageBox = new HBox();
        messageBox.setSpacing(10);
        messageBox.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        TextFlow messageBubble = new TextFlow(new Text(message));
        messageBubble.setPadding(new Insets(10));
        messageBubble.setMaxWidth(250);
        messageBubble.setStyle(isUser
                ? "-fx-background-color: #DFF0D8; -fx-background-radius: 5;"
                : "-fx-background-color: #F2DEDE; -fx-background-radius: 5;");

        ImageView avatar = new ImageView(new Image(isUser
                ? "https://via.placeholder.com/30/00FF00/FFFFFF?text=U"
                : "https://via.placeholder.com/30/0000FF/FFFFFF?text=F"));
        avatar.setFitWidth(30);
        avatar.setFitHeight(30);

        if (isUser) {
            messageBox.getChildren().addAll(messageBubble, avatar);
        } else {
            messageBox.getChildren().addAll(avatar, messageBubble);
        }

        chatArea.getChildren().add(messageBox);
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

    private Popup createEmojiPicker() {
        Popup popup = new Popup();
        GridPane emojiGrid = new GridPane();
        emojiGrid.setPadding(new Insets(10));
        emojiGrid.setHgap(5);
        emojiGrid.setVgap(5);
        String[] emojis = {"😊", "😂", "❤️", "👍", "🎉", "😢", "😡", "🤔", "🙌", "😎"};
        for (int i = 0; i < emojis.length; i++) {
            Button emojiButton = new Button(emojis[i]);
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
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            clientSocket.sendMessage("File shared: " + file.getName());
            addMessage("You shared a file: " + file.getName(), true);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
