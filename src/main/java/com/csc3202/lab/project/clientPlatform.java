package com.csc3202.lab.project;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class clientPlatform extends Application {
    private ChatClientSocket chatClientSocket;
    private String username;
    private TextArea chatArea;
    private TextField messageField;
    private ListView<String> userList;
    private Label messageTypeLabel;
    private ToggleButton privateMessageToggle;
    private Connection connection;
    private PreparedStatement selectStmt;
    private PreparedStatement updateStmt;
    private TextField searchField;
    private TextField usernameField;
    private TextField passwordField;
    private Button searchButton;
    private TextArea resultArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override 
    //Login GUI interface
    public void start(Stage primaryStage) {
        initializeDB(); // invoke database
        // Login dialog
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Chat Login");
        dialog.setHeaderText("Enter your username:");
        dialog.setContentText("Username:");
        dialog.showAndWait().ifPresent(name -> {
            username = name;
            initializeSocketConnection("localhost", 9000); // Initialize the socket connection
            createAndShowGUI(primaryStage); // Create and display the GUI
        });
    }
    // Main page after login
    private void createAndShowGUI(Stage primaryStage) {
        searchField = new TextField();
        searchField.setPromptText("Enter keyword to search for messages...");

        searchButton = new Button("Search");
        resultArea = new TextArea();
        resultArea.setEditable(false);  // Make the result area read-only

        // Add button action to search messages
        searchButton.setOnAction(e -> searchMessages());


        

        // Message sending logic
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());
        // Logic for closing the application
        primaryStage.setOnCloseRequest(e -> {
            try {
                chatClientSocket.sendMessage(new chatMessage(username, "has disconnected.", null, chatMessage.MessageType.USER_LEAVE));
                Platform.exit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Chat Client - " + username);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    // DON'T TOUCH THE DOWN PART FOR SOCKET
    private void initializeSocketConnection(String host, int port) {
        try {
            chatClientSocket = new ChatClientSocket(host, port, username);
            chatClientSocket.setChatArea(chatArea); // Pass UI components to the socket
            chatClientSocket.setUserList(userList); // Pass user list component to the socket
        } catch (Exception e) {
            showError("Connection Error", "Failed to connect to the server: " + e.getMessage());
        }
    }
    private void sendMessage() {
        try {
            String content = messageField.getText().trim();
            if (!content.isEmpty()) {
                String selectedUser = userList.getSelectionModel().getSelectedItem();
                chatMessage.MessageType type = (!privateMessageToggle.isSelected() || selectedUser == null)
                        ? chatMessage.MessageType.PUBLIC
                        : chatMessage.MessageType.PRIVATE;

                chatMessage message = new chatMessage(username, content, selectedUser, type);
                chatClientSocket.sendMessage(message);

                // Update the local UI
                Platform.runLater(() -> chatArea.appendText("[" + message.getTimestamp() + "] You: " + content + "\n"));
                messageField.clear();
            }
        } catch (Exception e) {
            showError("Error", "Failed to send message: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    // Initialize database
    public void initializeDB(){
        
            String dburl="jdbc:oracle:thin@localhost:1521:xe";
            String username="A222333";
            String password="222333";
        try{
            connection=DriverManager.getConnection(dburl,username,password);     
            System.out.print("Connected to Oracle database server");

        }catch (SQLException e){
            System.out.println("Error:");
            e.printStackTrace();
        }
        
    }
     // Method to search messages based on the entered keyword
    public void searchMessages() {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            resultArea.setText("Please enter a keyword to search.");
            return;
        }

        String sql = "SELECT username, email, message FROM client WHERE message LIKE ?";
        
        try (PreparedStatement selectStmt = connection.prepareStatement(sql)) {
            selectStmt.setString(1, "%" + keyword + "%");
            ResultSet resultSet = selectStmt.executeQuery();

            StringBuilder resultText = new StringBuilder();
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                String message = resultSet.getString("message");

                resultText.append("Username: ").append(username)
                        .append("\nEmail: ").append(email)
                        .append("\nMessage: ").append(message)
                        .append("\n\n");
            }

            // If no results, show a message
            if (resultText.length() == 0) {
                resultText.append("No results found for keyword: ").append(keyword);
            }

            // Display the results in the TextArea
            resultArea.setText(resultText.toString());

        } catch (SQLException e) {
            resultArea.setText("Error searching the database: " + e.getMessage());
        }
    }
     // Method to validate login credentials
     public void login() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            resultArea.setText("Please enter both username and password.");
            return;
        }

        // SQL query to check if the username exists and the password matches
        String sql = "SELECT password FROM client_profile WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();

            // If username exists, check the password
            if (resultSet.next()) {
                String storedPassword = resultSet.getString("password");

                // Check if the entered password matches the stored password
                if (storedPassword.equals(password)) {
                    resultArea.setText("Login successful! Welcome " + username);
                    // Proceed to the next part of the application (e.g., enabling chat features)
                } else {
                    resultArea.setText("Invalid password. Please try again.");
                }
            } else {
                resultArea.setText("Username not found. Please try again.");
            }

        } catch (SQLException e) {
            resultArea.setText("Error during login: " + e.getMessage());
        }
    }

    // Method to update the message for the username (on send)
    public void saveMessage() {
        String username = usernameField.getText().trim();
        String message = messageField.getText().trim();

        if (username.isEmpty() || message.isEmpty()) {
            resultArea.setText("Please enter both username and message.");
            return;
        }

        // Update the client_message table
        String sql = "INSERT INTO client_message (username, message) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, message);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                resultArea.setText("Message sent successfully from " + username);
            } else {
                resultArea.setText("Error sending message.");
            }
        } catch (SQLException e) {
            resultArea.setText("Error sending message: " + e.getMessage());
        }
    }
}

    

    
