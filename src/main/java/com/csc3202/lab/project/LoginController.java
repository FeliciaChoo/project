package com.csc3202.lab.project;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    private Button signUpButton;

    private Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @FXML
    private void handleSignIn() {
        String usernameInput = usernameField.getText().trim();
        String passwordInput = passwordField.getText().trim();

        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Both username and password are required.");
            return;
        }

        if (checkCredentials(usernameInput, passwordInput)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful!");
            // Transition to the main chat GUI (to be handled by your application logic)
            proceedToChat(usernameInput);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password.");
        }
    }

    @FXML
    private void handleSignUp() {
        String usernameInput = usernameField.getText().trim();
        String passwordInput = passwordField.getText().trim();

        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Both username and password are required.");
            return;
        }

        if (registerUser(usernameInput, passwordInput)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Username already exists.");
        }
    }

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

    private boolean registerUser(String username, String password) {
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

    private void proceedToChat(String username) {
        // Replace this with the actual logic to open the chat GUI
        System.out.println("Proceeding to chat for user: " + username);
    }
}
