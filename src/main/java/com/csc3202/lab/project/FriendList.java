package com.csc3202.lab.project;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendList extends Application {

    private List<Client> clients = new ArrayList<>();
    private String loggedInUser = "Alice"; // This would be dynamically set based on the logged-in user

    @Override
    public void start(Stage primaryStage) {
        // Fetch client data from the database
        fetchClientData();

        // Root BorderPane
        BorderPane root = new BorderPane();
        root.setPrefSize(324, 467);
        root.setStyle("-fx-background-color: #FFE4E1;");

        // Top section
        VBox topVBox = new VBox(5);
        topVBox.setAlignment(javafx.geometry.Pos.CENTER);

        Label titleLabel = new Label("Heart2Heart");
        titleLabel.setPrefSize(364, 48);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-color: #FFB6C1; -fx-text-fill: white; -fx-padding: 10px; -fx-alignment: center;");

        topVBox.getChildren().add(titleLabel);
        root.setTop(topVBox);

        // Center section with Friend List and Status
        VBox centerVBox = new VBox(10);
        centerVBox.setPadding(new Insets(10));

        // Friend List Section
        Label friendsLabel = new Label("Friends");
        friendsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        centerVBox.getChildren().add(friendsLabel);

        // Display each client (excluding logged-in user)
        for (Client client : clients) {
            if (!client.getUsername().equals(loggedInUser)) { // Exclude the logged-in user
                HBox friendBox = new HBox(10);
                friendBox.setStyle("-fx-padding: 5px;");

                // Circle with first letter of username
                Circle profilePic = new Circle(20, Color.LIGHTPINK);
                Text letter = new Text(client.getUsername().substring(0, 1).toUpperCase());
                letter.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: white;");
                profilePic.setCenterX(20);
                profilePic.setCenterY(20);
                profilePic.setRadius(20);
                letter.setTranslateX(5);
                letter.setTranslateY(-7);

                // User information
                VBox userInfo = new VBox();
                Label usernameLabel = new Label(client.getUsername());
                usernameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
                Label statusLabel = new Label("Status: " + client.getStatus());
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
                userInfo.getChildren().addAll(usernameLabel, statusLabel);

                friendBox.getChildren().addAll(profilePic, letter, userInfo);
                centerVBox.getChildren().add(friendBox);
            }
        }

        // Status Section
        Label statusGridLabel = new Label("Status");
        statusGridLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        centerVBox.getChildren().add(statusGridLabel);

        root.setCenter(centerVBox);

        // Bottom section with buttons
        VBox bottomVBox = new VBox();
        bottomVBox.setPrefSize(324, 50);
        bottomVBox.setStyle("-fx-background-color: #FFB6C1;");

        HBox bottomHBox = new HBox(10);
        bottomHBox.setAlignment(javafx.geometry.Pos.CENTER);
        bottomHBox.setPadding(new Insets(10));

        Button friendListButton = new Button("Friend List");
        friendListButton.setDisable(true);
        friendListButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button settingsButton = new Button("Settings");
        settingsButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        bottomHBox.getChildren().addAll( settingsButton,friendListButton);
        bottomVBox.getChildren().add(bottomHBox);
        root.setBottom(bottomVBox);

        // Scene and Stage setup
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Heart2Heart UI");
        primaryStage.show();
    }

    private void fetchClientData() {
        String url = "jdbc:oracle:thin:@fsktmdbora.upm.edu.my:1521:FSKTM"; // Replace with actual DB connection details
        String username = "your_db_username"; // Replace with actual DB username
        String password = "your_db_password"; // Replace with actual DB password
        String query = "SELECT username, status FROM client_profile";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Clear the existing clients list
            clients.clear();

            // Fetch data from the ResultSet
            while (rs.next()) {
                String user = rs.getString("username");
                String status = rs.getString("status");
                clients.add(new Client(user, status));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Client class to store username and status
    static class Client {
        private String username;
        private String status;

        public Client(String username, String status) {
            this.username = username;
            this.status = status;
        }

        public String getUsername() {
            return username;
        }

        public String getStatus() {
            return status;
        }
    }
}

