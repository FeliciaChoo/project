package com.csc3202.lab.project;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.io.IOException;

public class Platform extends Application {
    private ChatClientSocket chatClientSocket;
    private TextField messageInput;
    private VBox chatArea;
    private String username;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // 获取用户名并连接服务器
        String serverAddress = "localhost";  // 可以在启动时更改服务器地址
        username = "User_" + (int)(Math.random() * 1000);  // 设置一个随机的用户名

        try {
            // 创建客户端 socket 和 UI
            chatClientSocket = new ChatClientSocket(serverAddress, 12345, username);

            // 初始化聊天界面
            initializeUI();

            // 启动接收消息的线程
            receiveMessages();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 创建场景并启动应用
        Scene scene = new Scene(createMainLayout(), 600, 600);  // 增加宽度来放置好友列表
        scene.setFill(javafx.scene.paint.Color.PINK);  // 设置整个窗口背景为粉色
        primaryStage.setTitle("Heart2Heart");  // 修改标题名称为 Heart2Heart
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // 创建主界面布局
    private BorderPane createMainLayout() {
        BorderPane mainLayout = new BorderPane();

        // 设置根布局的背景颜色为粉色
        mainLayout.setStyle("-fx-background-color: #FFC0CB;");

        // 1. 创建聊天区域
        ScrollPane chatScroll = new ScrollPane();
        chatArea = new VBox();
        chatArea.setSpacing(10);
        chatArea.setPadding(new Insets(10));
        chatScroll.setContent(chatArea);
        chatScroll.setFitToWidth(true);
        chatScroll.setStyle("-fx-background-color: transparent;");

        // 2. 创建好友列表
        ListView<String> friendList = new ListView<>();
        friendList.setPrefWidth(150);
        friendList.getItems().addAll("Friend 1", "Friend 2", "Friend 3", "Friend 4");  // 示例数据
        friendList.setStyle("-fx-background-color: #FFC0CB; -fx-border-color: #FFC0CB;");

        // 将聊天区域放置在中间，好友列表放置在右边
        mainLayout.setCenter(chatScroll);
        mainLayout.setRight(friendList);

        // 3. 设置底部的输入框和发送按钮
        HBox inputArea = new HBox();
        inputArea.setSpacing(10);
        inputArea.setPadding(new Insets(10));
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setStyle("-fx-background-color: #FFFFFF;");

        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setPrefWidth(150);
        messageInput.setStyle("-fx-background-color: #FFF0F5; -fx-border-radius: 10px;");

        Button sendButton = new Button("⬆");
        sendButton.setStyle("-fx-font-size: 16px; -fx-background-color: #FFB6C1; -fx-border-radius: 5px;");
        sendButton.setOnAction(event -> sendMessage());

        inputArea.getChildren().addAll(messageInput, sendButton);

        mainLayout.setBottom(inputArea);

        return mainLayout;
    }

    // 初始化聊天界面
    private void initializeUI() {
        // 这部分代码不再需要，因为我们已经将UI布局整合到 createMainLayout 中
    }

    // 启动线程接收消息
    private void receiveMessages() {
        new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = chatClientSocket.receiveMessage()) != null) {
                    // 更新UI界面显示消息
                    String finalServerMessage = serverMessage;
                    javafx.application.Platform.runLater(() -> updateChat(finalServerMessage));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 更新聊天界面
    private void updateChat(String message) {
        // Add received message to chat area
        addMessage(message, false);
    }

    // 发送消息
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            addMessage(message, true);  // Add the message to chat
            messageInput.clear();  // Clear the input field

            // Send message to server
            chatClientSocket.sendMessage(message);
        }
    }

    // 添加消息到聊天区域
    private void addMessage(String message, boolean isUser) {
        HBox messageBox = new HBox();
        messageBox.setSpacing(10);
        messageBox.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        // 用户头像（示例，实际可替换为动态头像）
        ImageView avatar = new ImageView(new Image(isUser ? "https://via.placeholder.com/30/00FF00/FFFFFF?text=U" : "https://via.placeholder.com/30/0000FF/FFFFFF?text=F"));
        avatar.setFitWidth(30);
        avatar.setFitHeight(30);

        // 消息气泡
        TextFlow messageBubble = new TextFlow(new Text(message));
        messageBubble.setPadding(new Insets(10));
        messageBubble.setStyle(isUser
                ? "-fx-background-color: #FFB6C1; -fx-background-radius: 10;"  // 用户消息背景色为粉色
                : "-fx-background-color: #FFFFFF; -fx-background-radius: 10;");  // 朋友消息背景色为白色
        messageBubble.setMaxWidth(250);

        if (isUser) {
            messageBox.getChildren().addAll(messageBubble, avatar);
        } else {
            messageBox.getChildren().addAll(avatar, messageBubble);
        }

        // Add message box to chat area
        chatArea.getChildren().add(messageBox);
    }
}
