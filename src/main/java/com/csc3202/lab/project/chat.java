package com.csc3202.lab.project;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class Chat extends Application {
    private VBox chatArea;
    private TextField messageInput;
    private String username; // 用于存储随机生成的用户名
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    public void start(Stage stage) {
        // 随机生成用户名
        username = generateRandomUsername();

        // 连接到服务器
        connectToServer();

        // 主界面布局
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

        Circle avatar = loadAvatar(null);
        Text friendLabel = new Text(username); // 显示随机生成的用户名
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

        // 表情包按钮
        Button emojiButton = new Button();
        ImageView emojiIcon = new ImageView(new Image("file:C:/Users/HP/Desktop/project-main/屏幕截图 2025-01-21 185634.png")); // 本地表情图片路径
        emojiIcon.setFitWidth(20);
        emojiIcon.setFitHeight(20);
        emojiButton.setGraphic(emojiIcon);
        emojiButton.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #FF69B4; -fx-border-radius: 20; -fx-alignment: center;");
        Popup emojiPicker = createEmojiPicker();
        emojiButton.setOnAction(event -> emojiPicker.show(messageInput.getScene().getWindow()));

        // 文本输入框
        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setPrefWidth(250);
        messageInput.setPrefHeight(40);

        // 上传图片按钮
        Button uploadButton = new Button();
        ImageView cameraIcon = new ImageView(new Image("file:C:/Users/HP/Desktop/project-main/屏幕截图 2025-01-21 185640.png")); // 本地摄像头图片路径
        cameraIcon.setFitWidth(20);
        cameraIcon.setFitHeight(20);
        uploadButton.setGraphic(cameraIcon);
        uploadButton.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #FF69B4; -fx-border-radius: 20; -fx-alignment: center;");
        uploadButton.setOnAction(event -> handleFileSelection());

        // 发送按钮
        Button sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-border-radius: 5; -fx-font-size: 14px;");
        sendButton.setPrefHeight(40);
        sendButton.setOnAction(event -> sendMessage());

        inputArea.getChildren().addAll(emojiButton, messageInput, uploadButton, sendButton);

        root.getChildren().addAll(topBar, chatScrollPane, inputArea);

        Scene scene = new Scene(root, 400, 600);
        stage.setMinWidth(400);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.setTitle("HEART2HEART - " + username);
        stage.show();

        new Thread(this::listenForMessages).start();
    }

    private void connectToServer() {
        try {
            socket = new Socket("127.0.0.1", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(username + " has joined the chat.");
            System.out.println("Connected to server.");
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            out.println(username + ": " + message);
            messageInput.clear();
        }
    }

    private void listenForMessages() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message);
                String finalMessage = message;
                Platform.runLater(() -> addMessage(finalMessage, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
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

        messageBox.getChildren().add(messageBubble);
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
            out.println(message);
            addMessage(message, true);
        }
    }

    private Circle loadAvatar(String imagePath) {
        Circle circle = new Circle(15);
        circle.setFill(Color.LIGHTPINK);
        return circle;
    }

    private String generateRandomUsername() {
        return "User_" + UUID.randomUUID().toString().substring(0, 8); // 使用 UUID 生成唯一用户名
    }

    public static void main(String[] args) {
        launch(args);
    }
}
