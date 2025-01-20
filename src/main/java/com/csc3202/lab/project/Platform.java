package com.csc3202.lab.project;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

public class Platform extends Application {
    private ChatClientSocket chatClientSocket;
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
            final ChatClientUI chatClientUI = new ChatClientUI(chatClientSocket); // 使用 final 修饰
            chatClientUI.start(primaryStage); // 启动UI

            // 启动接收消息的线程
            receiveMessages(chatClientUI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 启动线程接收消息
    private void receiveMessages(final ChatClientUI chatClientUI) { // 使用 final 修饰
        new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = chatClientSocket.receiveMessage()) != null) {
                    // 更新UI界面显示消息
                    String finalServerMessage = serverMessage;
                    javafx.application.Platform.runLater(() -> chatClientUI.updateChat(finalServerMessage));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
