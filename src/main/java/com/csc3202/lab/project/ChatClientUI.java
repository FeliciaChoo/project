package com.csc3202.lab.project;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ChatClientUI {
    private TextArea chatArea; // 显示聊天记录的区域
    private TextField inputField; // 输入框
    private Button sendButton; // 发送按钮
    private StringProperty messageProperty = new SimpleStringProperty(); // 用于绑定输入框的消息内容

    private ChatClientSocket chatClientSocket; // 客户端核心逻辑

    public ChatClientUI(ChatClientSocket chatClientSocket) {
        this.chatClientSocket = chatClientSocket;
    }

    public void start(Stage primaryStage) {
        // 创建显示聊天记录的TextArea
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(300);
        chatArea.setStyle("-fx-font-size: 14px;");

        // 创建消息输入框
        inputField = new TextField();
        inputField.setPromptText("Type your message...");
        inputField.setStyle("-fx-font-size: 14px;");
        inputField.textProperty().bindBidirectional(messageProperty);

        // 创建发送按钮
        sendButton = new Button("Send");
        sendButton.setStyle("-fx-font-size: 14px;");
        sendButton.setOnAction(e -> sendMessage(messageProperty.get()));

        // 创建布局
        HBox inputBox = new HBox(10, inputField, sendButton);
        inputBox.setPadding(new Insets(10));
        inputBox.setStyle("-fx-background-color: #f5f5f5;");

        VBox root = new VBox(10, new ScrollPane(chatArea), inputBox);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #ffffff;");

        Scene scene = new Scene(root, 400, 450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat Application");
        primaryStage.show();
    }

    // 发送消息到服务器
    private void sendMessage(String message) {
        if (message != null && !message.trim().isEmpty()) {
            chatClientSocket.sendMessage(message);
            messageProperty.set(""); // 清空输入框
        }
    }

    // 更新聊天记录
    public void updateChat(String message) {
        chatArea.appendText(message + "\n");
        chatArea.setScrollTop(Double.MAX_VALUE); // 滚动到最新消息
    }
}
