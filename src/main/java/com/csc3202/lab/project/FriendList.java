package com.csc3202.lab.project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.image.Image;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendList {

    private Connection connection;
    private List<Client> clients = new ArrayList<>();
    private String loggedInUser = "Alice"; // This would be dynamically set based on the logged-in user
    private BorderPane root;

    public FriendList() {
        root = new BorderPane();
        root.setPrefSize(300, 457);
        root.setStyle("-fx-background-color: #FFE4E1;");
        initializeUI();
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        fetchClientData(); // Fetch data once connection is set
        updateUI(); // Update UI with fetched data
    }

    public BorderPane getRoot() {
        return root;
    }

    private void initializeUI() {
        // Top section
        VBox topVBox = new VBox(5);
        topVBox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Heart2Heart");
        titleLabel.setPrefSize(364, 48);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-color: #FFB6C1; -fx-text-fill: white; -fx-padding: 10px; -fx-alignment: center;");

        topVBox.getChildren().add(titleLabel);
        root.setTop(topVBox);

        // Center section placeholder
        VBox centerVBox = new VBox(10);
        centerVBox.setPadding(new Insets(10));
        root.setCenter(centerVBox);
    }

    private void fetchClientData() {
        if (connection == null) {
            throw new IllegalStateException("Database connection is not set");
        }

        // SQL query to fetch client profiles excluding the logged-in user
        String query = "SELECT username, email, status, profile_image_path " +
                "FROM client_profile " +
                "WHERE username != ?";  // Exclude logged-in user

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, loggedInUser);  // Exclude the logged-in user from the list

            try (ResultSet rs = stmt.executeQuery()) {
                // Clear the existing clients list
                clients.clear();

                // Fetch data from the ResultSet
                while (rs.next()) {
                    String user = rs.getString("username");
                    String email = rs.getString("email");
                    String status = rs.getString("status");
                    String imagePath = rs.getString("profile_image_path");
                    clients.add(new Client(user, email, status, imagePath));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateUI() {
        VBox centerVBox = new VBox(10);
        centerVBox.setPadding(new Insets(10));

        // Friend List Section
        Label friendsLabel = new Label("Friends");
        friendsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        centerVBox.getChildren().add(friendsLabel);

        // Display the client profiles
        for (Client client : clients) {
            HBox friendBox = new HBox(10);
            friendBox.setStyle("-fx-padding: 5px;");
            friendBox.setAlignment(Pos.CENTER_LEFT); // Align children to the left

            // Circle with profile image (if available)
            Circle profilePic = new Circle(20);
            if (client.getProfileImagePath() != null) {
                try {
                    Image image = new Image("file:" + client.getProfileImagePath());
                    profilePic.setFill(new ImagePattern(image));
                } catch (Exception e) {
                    // Handle case where image path is invalid or not available
                    profilePic.setFill(Color.LIGHTPINK);
                }
            } else {
                profilePic.setFill(Color.LIGHTPINK); // Default color if no image
            }

            VBox userInfo = new VBox();
            Label usernameLabel = new Label(client.getUsername());
            usernameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
            Label emailLabel = new Label("Email: " + client.getEmail());
            emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
            Label statusLabel = new Label("Status: " + client.getStatus());
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
            userInfo.getChildren().addAll(usernameLabel, emailLabel, statusLabel);

            // Spacer to push "Chat" button to the right
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Chat button to open the chat screen
            Button chatButton = new Button("Chat");
            chatButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white;");
            chatButton.setOnAction(e -> openChatScreen(client.getUsername()));

            // Add profile, user info, and chat button with spacer
            friendBox.getChildren().addAll(profilePic, userInfo, spacer, chatButton);
            centerVBox.getChildren().add(friendBox);
        }

        root.setCenter(centerVBox);
    }


    // Method to open the chat screen for the selected friend
    private void openChatScreen(String friendUsername) {
        System.out.println("Opening chat screen with " + friendUsername);
    
        // Fetch the friend's image path from the database
        String friendImagePath = getFriendImagePath(friendUsername);
    
        // Pass both username and image path to the Chat constructor
        Chat chatApp = new Chat(friendUsername, friendImagePath);
        root.setCenter(chatApp.getRoot()); // Update the center with the chat screen
    }
    
    private String getFriendImagePath(String friendUsername) {
        String imagePath = null;
        String query = "SELECT profile_image_path FROM client_profile WHERE username = ?";
    
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, friendUsername);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    imagePath = rs.getString("profile_image_path");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return imagePath;
    }
    


    // Client class to store username, email, status, and profile image path
    static class Client {
        private String username;
        private String email;
        private String status;
        private String profileImagePath;

        public Client(String username, String email, String status, String profileImagePath) {
            this.username = username;
            this.email = email;
            this.status = status;
            this.profileImagePath = profileImagePath;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getStatus() {
            return status;
        }

        public String getProfileImagePath() {
            return profileImagePath;
        }
    }
}
