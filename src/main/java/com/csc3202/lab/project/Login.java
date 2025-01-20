package com.csc3202.lab.project;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.sql.*;
import java.util.Base64;

public class Login extends Application {

    private static final String ENCRYPTION_KEY = "1234567890123456"; // 16-byte key
    private static final String ENCRYPTION_IV = "abcdefghijklmnop"; // 16-byte IV
    private Connection connection;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@fsktmdbora.upm.edu.my:1521:FSKTM",
                    "A222333",
                    "222333"
            );
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the database.");
            return;
        }

        // Create BorderPane root layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #FFE4E1;");

        // Title Label
        Label titleLabel = new Label("Welcome to Heart2Heart");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-color: #FFB6C1; -fx-text-fill: white; -fx-padding: 10px;");
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        root.setTop(titleLabel);

        // GridPane for login
        GridPane gridPane = new GridPane();
        gridPane.setVgap(15);
        gridPane.setHgap(10);
        gridPane.setStyle("-fx-padding: 10px;");

        // Username and password fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-border-color: pink; -fx-border-radius: 5; -fx-padding: 5;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-border-color: pink; -fx-border-radius: 5; -fx-padding: 5;");

        gridPane.add(new Label("Username:"), 0, 0);
        gridPane.add(usernameField, 1, 0);
        gridPane.add(new Label("Password:"), 0, 1);
        gridPane.add(passwordField, 1, 1);

        // Login and Sign-Up buttons
        Button logInButton = new Button("Log In");
        logInButton.setOnAction(e -> handleLogIn(usernameField.getText(), passwordField.getText()));

        Button signInButton = new Button("Sign In");
        signInButton.setOnAction(e -> handleSignIn(usernameField.getText(), passwordField.getText()));

        VBox vBox = new VBox(20, gridPane, logInButton, new Label("Don't have an account?"), signInButton);
        vBox.setAlignment(Pos.CENTER);
        root.setCenter(vBox);

        // Set up the scene
        Scene scene = new Scene(root, 350, 610);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Heart2Heart Login");
        primaryStage.show();
    }

    private void handleLogIn(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Both username and password are required.");
            return;
        }

        if (checkCredentials(username, password)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful!");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password.");
        }
    }

    private void handleSignIn(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Both username and password are required.");
            return;
        }

        storeNewUser(username, password);
    }

    private boolean checkCredentials(String username, String password) {
        String sql = "SELECT * FROM A222333.client_login WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, encrypt(username));
            stmt.setString(2, encrypt(password));
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException | Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while checking credentials.");
            return false;
        }
    }

    private void storeNewUser(String username, String password) {
        String sql = "INSERT INTO A222333.client_login (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, encrypt(username));
            stmt.setString(2, encrypt(password));
            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "User registered successfully!");
        } catch (SQLException | Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while registering the user.");
        }
    }

    private String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKey key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
        IvParameterSpec iv = new IvParameterSpec(ENCRYPTION_IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

