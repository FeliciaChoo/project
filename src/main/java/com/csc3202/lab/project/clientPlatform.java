package com.csc3202.lab.project;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
    private PasswordField passwordField;  // Password field
    private Button signInButton;
    private Button signUpButton;
    private TextArea resultArea;

    public static void main(String[] args) {
        launch(args);
    }

    @Override 
    //Login GUI interface
    public void start(Stage primaryStage) {
        initializeDB(); // Initialize the database connection
        createLoginDialog(primaryStage); // Create the login dialog
    }

    // Create the login dialog with both username and password fields
    private void createLoginDialog(Stage primaryStage) {
        // Create the login dialog layout
        BorderPane loginLayout = new BorderPane();
        VBox loginBox = new VBox(10);
        loginBox.setPadding(new Insets(20));

        // Username and password fields
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        // Buttons for sign in and sign up
        signInButton = new Button("Sign In");
        signUpButton = new Button("Sign Up");

        // Add the fields and buttons to the layout
        loginBox.getChildren().addAll(new Label("Username:"), usernameField, new Label("Password:"), passwordField, signInButton, signUpButton);
        loginLayout.setCenter(loginBox);

        // Event handlers for buttons
        signInButton.setOnAction(e -> signInAction(primaryStage));
        signUpButton.setOnAction(e -> signUpAction());

        // Show the login dialog
        Scene loginScene = new Scene(loginLayout, 400, 250);
        primaryStage.setTitle("Chat Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    // Handle sign-in action
    private void signInAction(Stage primaryStage) {
        String usernameInput = usernameField.getText().trim();
        String passwordInput = passwordField.getText().trim();

        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            showError("Error", "Both username and password are required.");
            return;
        }

        // Check the credentials against the database
        if (checkCredentials(usernameInput, passwordInput)) {
            username = usernameInput;
            initializeSocketConnection("localhost", 9000); // Initialize the socket connection
            createAndShowGUI(primaryStage); // Create and display the chat GUI
        } else {
            showError("Error", "Invalid username or password.");
        }
    }

    // Handle sign-up action
    private void signUpAction() {
        String usernameInput = usernameField.getText().trim();
        String passwordInput = passwordField.getText().trim();

        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            showError("Error", "Both username and password are required.");
            return;
        }

        // Store the new user credentials in the database
        if (registerUser(usernameInput, passwordInput)) {
            showMessage("Success", "Account created successfully.");
        } else {
            showError("Error", "Username already exists.");
        }
    }

    // Method to check if the credentials are valid (existing user)
    private boolean checkCredentials(String username, String password) {
        String sql = "SELECT * FROM client_profile WHERE username = ? AND password = ?";
        try (PreparedStatement selectStmt = connection.prepareStatement(sql)) {
            selectStmt.setString(1, username);
            selectStmt.setString(2, password);
            ResultSet resultSet = selectStmt.executeQuery();
            return resultSet.next(); // If user exists, it returns true
        } catch (SQLException e) {
            showError("Database Error", "Error while verifying credentials: " + e.getMessage());
            return false;
        }
    }

    // Method to register a new user in the database
    private boolean registerUser(String username, String password) {
        String sql = "INSERT INTO client_profile (username, password) VALUES (?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(sql)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            int rowsAffected = insertStmt.executeUpdate();
            return rowsAffected > 0; // Return true if registration is successful
        } catch (SQLException e) {
            showError("Database Error", "Error while creating the account: " + e.getMessage());
            return false;
        }
    }

    // Method to show error message in a dialog
    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // Method to show success message in a dialog
    private void showMessage(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    // Create and show the chat GUI after login
    private void createAndShowGUI(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Initialize components first
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);

        messageField = new TextField();
        messageField.setPromptText("Type your message...");

        userList = new ListView<>();
        userList.setPrefWidth(150);

        // Top toolbar
        ToolBar toolbar = new ToolBar();
        Button backButton = new Button("Logout");
        Button profileButton = new Button("Profile");
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Online", "Away", "Busy", "Offline");
        statusCombo.setValue("Online");
        
        toolbar.getItems().addAll(backButton, profileButton, statusCombo);
        root.setTop(toolbar);

        // Enhanced message input area
        VBox messageBox = new VBox(5);
        HBox controlsBox = new HBox(10);
        
        messageTypeLabel = new Label("To: Everyone");
        privateMessageToggle = new ToggleButton("Private Message");
        Button sendButton = new Button("Send");
        
        // Style the controls
        messageTypeLabel.setStyle("-fx-font-weight: bold;");
        privateMessageToggle.setStyle("-fx-background-radius: 15;");
        
        controlsBox.getChildren().addAll(messageTypeLabel, privateMessageToggle);
        HBox inputBox = new HBox(10);
        inputBox.getChildren().addAll(messageField, sendButton);
        messageBox.getChildren().addAll(controlsBox, inputBox);
        
        // Layout
        VBox centerBox = new VBox(10);
        centerBox.getChildren().addAll(chatArea, messageBox);
        root.setCenter(centerBox);
        root.setRight(userList);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Chat Client - " + username);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeSocketConnection(String host, int port) {
        try {
            chatClientSocket = new ChatClientSocket(host, port, username);
            chatClientSocket.setChatArea(chatArea); // Pass UI components to the socket
            chatClientSocket.setUserList(userList); // Pass user list component to the socket
        } catch (Exception e) {
            showError("Connection Error", "Failed to connect to the server: " + e.getMessage());
        }
    }

    public void initializeDB() {
        String dburl = "jdbc:oracle:thin:@fsktmdbora.upm.edu.my:1521:FSKTM";  // Use the correct Oracle JDBC URL
        String username = "A222333";
        String password = "222333";
        
        try {
            // Load the Oracle JDBC driver class (not your custom 'Chat' class)
            Class.forName("oracle.jdbc.OracleDriver");  // This loads the Oracle JDBC Driver
            
            // Establish the connection to the database
            connection = DriverManager.getConnection(dburl, username, password);
            System.out.println("Connected to Oracle database server");
            
        } catch (SQLException e) {
            System.out.println("Error during database connection:");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Oracle JDBC Driver not found. Ensure ojdbc8.jar is in the classpath.");
            e.printStackTrace();
        }
    }

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
}
