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
    private String loggedInUser; // This will be set dynamically based on the logged-in user
    private BorderPane root;
    private Main mainApp; // Reference to the main application


    // Constructor to initialize FriendList with the logged-in user's username
    public FriendList(String loggedInUser, Main mainApp) {
        this.loggedInUser = loggedInUser;
        this.mainApp = mainApp; // Store the reference to the main application
        root = new BorderPane();
        root.setPrefSize(300, 457);
        root.setStyle("-fx-background-color: #FFE4E1;");
        initializeUI();
    }

    // Method to set the database connection and fetch client data
    public void setConnection(Connection connection) {
        this.connection = connection;
        fetchClientData(); // Fetch data once connection is set
        updateUI(); // Update UI with fetched data
    }

    // Method to get the root UI component
    public BorderPane getRoot() {
        if (root == null) {
            root = new BorderPane();
            root.setPrefSize(300, 457);
            root.setStyle("-fx-background-color: #FFE4E1;");
            initializeUI();
        }
        return root;
    }

    // Method to initialize the UI components (layout and styles)
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

    // Method to fetch the client data from the database excluding the logged-in user
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

    // Method to update the UI with the fetched client data
    private void updateUI() {
        VBox centerVBox = new VBox(10);
        centerVBox.setPadding(new Insets(10));

        // Friend List Section
        Label friendsLabel = new Label("Friends");
        friendsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        centerVBox.getChildren().add(friendsLabel);
        // Create a Group Chat button beside the Friends label
Button groupChatButton = new Button("Group Chat");
groupChatButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white;");
groupChatButton.setOnAction(e -> openChatScreen(null, true));

// Add the Group Chat button to the center VBox (beside Friends label)
HBox headerBox = new HBox(10);
headerBox.setAlignment(Pos.CENTER_LEFT);
headerBox.getChildren().addAll(friendsLabel, groupChatButton);

// Add headerBox (containing Friends label and Group Chat button) to the center VBox
centerVBox.getChildren().add(headerBox);

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
            chatButton.setOnAction(e -> openChatScreen(client.getUsername(),false));

            // Add profile, user info, and chat button with spacer
            friendBox.getChildren().addAll(profilePic, userInfo, spacer, chatButton);
            centerVBox.getChildren().add(friendBox);
        }

        root.setCenter(centerVBox);
    }

    // Method to open the chat screen with a selected friend
    // Method to open the chat screen with a selected friend or group
private void openChatScreen(String friendUsername, boolean isGroupChat) {
    System.out.println("Opening " + (isGroupChat ? "group" : "private") + " chat screen");

    // Create Chat instance based on the type of chat (group or private)
    String serverAddress = "127.0.0.1"; // Replace with actual server address
    int serverPort = 12345; // Replace with actual server port

    // If it's a group chat, pass "Group Chat" as the friendUsername
    if (isGroupChat) {
        Chat chatApp = new Chat(loggedInUser, "Group Chat", null, serverAddress, serverPort, isGroupChat, mainApp);
        VBox chatRoot = chatApp.getRoot();
        if (chatRoot != null) {
            root.setCenter(chatRoot);
        } else {
            System.err.println("Failed to load group chat UI");
        }
    } else {
        // If it's a private chat, pass the friend's username
        String friendImagePath = getFriendImagePath(friendUsername); // Fetch friend's profile image
        Chat chatApp = new Chat(loggedInUser, friendUsername, friendImagePath, serverAddress, serverPort, isGroupChat,mainApp);
        VBox chatRoot = chatApp.getRoot();
        if (chatRoot != null) {
            root.setCenter(chatRoot);
        } else {
            System.err.println("Failed to load private chat UI");
        }
    }
}



    // Method to get the friend's image path from the database
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
