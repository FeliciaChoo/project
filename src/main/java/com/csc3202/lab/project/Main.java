package com.csc3202.lab.project;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

import java.sql.*;

public class Main extends Application {

    private Connection connection;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Initialize database connection
        initializeDB();

        // Launch the Login screen first
        Login loginApp = new Login(this);  // Pass 'this' to access main app once logged in
        loginApp.start(primaryStage);  // Start the login screen
    }

    // This method will be called after a successful login to show the main screen
    public void loadMainScreen(String username) {
        BorderPane root = new BorderPane();

        // Create Bottom HBox to hold the buttons
        HBox bottomHBox = new HBox(10);
        bottomHBox.setAlignment(Pos.CENTER);
        bottomHBox.setStyle("-fx-padding: 10px;-fx-background-color:#FFB6C1FF ");

        // Friend List Button
        Button friendListButton = new Button("Friend List");
        friendListButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        friendListButton.setOnAction(e -> loadFriendListScreen(root));

        // Settings Button
        Button settingsButton = new Button("Settings");
        settingsButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        settingsButton.setOnAction(e -> handleSettingsButton(root,username));

        bottomHBox.getChildren().addAll(settingsButton, friendListButton);
        root.setBottom(bottomHBox);

        // Set the initial content for the top section (can be changed based on button presses)
        VBox topContent = new VBox();
        topContent.setAlignment(Pos.CENTER);
        topContent.getChildren().add(new javafx.scene.control.Label("Welcome to Heart2Heart"));
        root.setCenter(topContent);

        // Set up the main scene
        Scene mainScene = new Scene(root, 350, 610);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Heart2Heart Main Screen");
        primaryStage.show();
    }

    private void loadFriendListScreen(BorderPane root) {
        if (connection != null) {
            FriendList friendList = new FriendList();  // Create a new instance of FriendList
            friendList.setConnection(connection);      // Pass the database connection
            root.setCenter(friendList.getRoot());     // Update the top content dynamically
        } else {
            System.err.println("Database connection is not established.");
        }
    }

    private void handleSettingsButton(BorderPane root, String username) {
        if (connection != null) {
            try {

                // Load the Profile.fxml file
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/settingGUI.fxml"));

                // Check if the FXML file exists
                if (loader.getLocation() == null) {
                    System.err.println("FXML file not found at: /view/settingGUI.fxml");
                    return;
                }

                Parent profileRoot = loader.load();

                // Get the controller and pass the database connection
                ProfileController profileController = loader.getController();
                if (profileController != null) {
                    profileController.setConnection(connection);
                } else {
                    System.err.println("ProfileController is null. Check the FXML file.");
                    return;
                }
                profileController.setUsername(username);
                // Update the center of the BorderPane with the profile view
                root.setCenter(profileRoot);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to load Profile.fxml: " + e.getMessage());
            }
        } else {
            System.err.println("Database connection is not established.");
        }
    }


    private void initializeDB() {
        try {
            String dbUrl = "jdbc:oracle:thin:@fsktmdbora.upm.edu.my:1521:FSKTM";
            String username = "A222333";
            String password = "222333";
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(dbUrl, username, password);
            System.out.println("Connected to Oracle database server.");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to database", e);
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
