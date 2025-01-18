package com.csc3202.lab.project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class Main extends Application {

    private Connection connection;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        if (initializeDB()) {
            Login loginApp = new Login(this);
            loginApp.start(primaryStage);
        } else {
            System.err.println("Application terminated due to database connection failure.");
            System.exit(1);
        }
    }

    public void loadMainScreen(String username) {
        BorderPane root = new BorderPane();

        // Create Bottom HBox for buttons
        HBox bottomHBox = new HBox(10);
        bottomHBox.setAlignment(Pos.CENTER);
        bottomHBox.setStyle("-fx-padding: 10px;-fx-background-color:#FFB6C1FF ");

        Button friendListButton = new Button("Friend List");
        friendListButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        friendListButton.setOnAction(e -> loadFriendListScreen(root));

        Button settingsButton = new Button("Settings");
        settingsButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        settingsButton.setOnAction(e -> handleSettingsButton(root, username));

        bottomHBox.getChildren().addAll(settingsButton, friendListButton);
        root.setBottom(bottomHBox);

        // Set the initial content for the top section
        VBox topContent = new VBox();
        topContent.setAlignment(Pos.CENTER);
        topContent.setStyle("-fx-background-color: #FFE4E1; -fx-padding: 20px;");

        Label welcomeLabel = new Label("Welcome to Heart2Heart");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #FF1493;");

        String logoPath = "file:/C:/Users/felic/OneDrive/Documents/Lab/Project/project/src/main/resources/assets/logo.png";
        try {
            Image logoImage = new Image(logoPath);
            ImageView logoView = new ImageView(logoImage);
            logoView.setFitWidth(100);
            logoView.setPreserveRatio(true);
            topContent.getChildren().addAll(welcomeLabel, logoView);
        } catch (Exception e) {
            System.err.println("Failed to load the logo image from: " + logoPath);
            e.printStackTrace();
        }

        root.setCenter(topContent);

        Scene mainScene = new Scene(root, 350, 610);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Heart2Heart Main Screen");
        primaryStage.show();
    }

    private void loadFriendListScreen(BorderPane root) {
        if (connection != null) {
            FriendList friendList = new FriendList();
            friendList.setConnection(connection);
            root.setCenter(friendList.getRoot());
        } else {
            System.err.println("Database connection is not established.");
        }
    }

    private void handleSettingsButton(BorderPane root, String username) {
        if (connection != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/settingGUI.fxml"));
                Parent profileRoot = loader.load();
                ProfileController profileController = loader.getController();
                if (profileController != null) {
                    profileController.setConnection(connection);
                    profileController.setUsername(username);
                } else {
                    System.err.println("ProfileController is null. Check the FXML file.");
                    return;
                }
                root.setCenter(profileRoot);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to load Profile.fxml: " + e.getMessage());
            }
        } else {
            System.err.println("Database connection is not established.");
        }
    }

    private boolean initializeDB() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter Oracle Database Username: ");
            String username = scanner.nextLine();

            System.out.print("Enter Oracle Database Password: ");
            String password = scanner.nextLine();

            String dbUrl = "jdbc:oracle:thin:@fsktmdbora.upm.edu.my:1521:FSKTM";
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(dbUrl, username, password);
            System.out.println("Connected to Oracle database server.");
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void stop() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
