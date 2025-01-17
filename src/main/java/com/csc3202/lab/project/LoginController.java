package com.csc3202.lab.project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signInButton;

    @FXML
    private Button logInButton;

    private Connection connection;
    private clientPlatform clientApp;  // Reference to the clientPlatform to switch scenes
    private Stage primaryStage;  // Store the reference to primaryStage
    
    public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
    }


    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setClientApp(clientPlatform clientApp) {
        this.clientApp = clientApp;  // Set the reference to clientPlatform for scene switching
    }

    // Login handler (for existing users)
    @FXML
    private void handleLogIn() {
        String usernameInput = usernameField.getText().trim();
        String passwordInput = passwordField.getText().trim();

        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Both username and password are required.");
            return;
        }

        if (checkCredentials(usernameInput, passwordInput)) {
            // Add null check and fallback
            if (clientApp != null) {
                clientApp.loadProfileSettingsScene();
            } else {
                System.err.println("ClientApp reference is null, falling back to local method");
                loadProfileSettingsScreen();  // Use the local method as fallback
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password.");
        }
    
    }

    // Check credentials in the database
    private boolean checkCredentials(String username, String password) {
        String sql = "SELECT * FROM client_login WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error while verifying credentials: " + e.getMessage());
            return false;
        }
    }

    // Sign-in handler (for creating new users)
    @FXML
    private void handleSignIn() {
        String usernameInput = usernameField.getText().trim();
        String passwordInput = passwordField.getText().trim();

        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Both username and password are required.");
            return;
        }

        if (checkIfUserExists(usernameInput)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Username already exists.");
            return;
        }

        if (createAccount(usernameInput, passwordInput)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Error while creating the account.");
        }
    }

    // Check if user already exists
    private boolean checkIfUserExists(String username) {
        String sql = "SELECT * FROM client_login WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error while checking username existence: " + e.getMessage());
            return false;
        }
    }

    // Create a new account
    private boolean createAccount(String username, String password) {
        String sql = "INSERT INTO client_login (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error while creating the account: " + e.getMessage());
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    // Login handler (for existing users)

// Method to load ProfileSettingsController
private void loadProfileSettingsScreen() {
    try {
        // Load ProfileSettingsController FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/settingGUI.fxml"));
        Scene profileScene = new Scene(loader.load());

        // Set up ProfileSettingsController with the current connection
        ProfileSettingsController profileController = loader.getController();
        profileController.setConnection(connection);  // Pass the DB connection

        // Switch to the ProfileSettings scene
        primaryStage.setScene(profileScene);  // Use the primaryStage to set the scene
        primaryStage.show();
    } catch (IOException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error", "Unable to load profile settings.");
    }
}
}