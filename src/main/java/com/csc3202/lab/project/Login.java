package com.csc3202.lab.project;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.*;

public class Login extends Application {

    private Main mainApp; // Reference to the Main class
    private Connection connection;
    private Stage primaryStage;

    // Constructor that accepts the Main app object
    public Login(Main mainApp) {
        this.mainApp = mainApp;
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:@fsktmdbora.upm.edu.my:1521:FSKTM", "A222333", "222333");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to connect to the database.");
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Create BorderPane root layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #FFE4E1;");

        // Title Label
        Label titleLabel = new Label("Welcome to Heart2Heart");
        titleLabel.setPrefHeight(48.0);
        titleLabel.setPrefWidth(358.0);
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-color: #FFB6C1; -fx-text-fill: white; -fx-padding: 10px; -fx-alignment: center;");
        BorderPane.setAlignment(titleLabel, Pos.CENTER);

        root.setTop(titleLabel);

        // GridPane for login
        GridPane gridPane = new GridPane();
        gridPane.setVgap(15);
        gridPane.setHgap(10);
        gridPane.setStyle("-fx-padding: 10px;");

        // Label for Username
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        GridPane.setColumnIndex(usernameLabel, 0);
        GridPane.setRowIndex(usernameLabel, 0);

        // Label for Password
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        GridPane.setColumnIndex(passwordLabel, 0);
        GridPane.setRowIndex(passwordLabel, 1);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle("-fx-border-color: pink; -fx-border-radius: 5; -fx-padding: 5;");
        usernameField.setTooltip(new Tooltip("Enter your username"));
        usernameField.setPrefWidth(200);

        // Password Field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-border-color: pink; -fx-border-radius: 5; -fx-padding: 5;");
        passwordField.setTooltip(new Tooltip("Enter your password"));
        usernameField.setPrefWidth(200);

        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameField, 1, 0);
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);

        // Login Button
        Button logInButton = new Button("Log In");
        logInButton.setOnAction(e -> handleLogIn(usernameField, passwordField));
        logInButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Sign In Button
        Button signInButton = new Button("Sign In");
        signInButton.setOnAction(e -> handleSignIn(usernameField, passwordField));
        signInButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label accountPromptLabel = new Label("Don't have an account?");
        accountPromptLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        VBox vBox = new VBox(20);
        vBox.setAlignment(Pos.CENTER);
        vBox.setStyle("-fx-padding: 20px;");
        vBox.getChildren().addAll(gridPane, logInButton, accountPromptLabel, signInButton);

        root.setCenter(vBox);

        // Set up the scene
        Scene scene = new Scene(root, 350, 610);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Heart2Heart Login");
        primaryStage.show();
    }

    private void handleLogIn(TextField usernameField, PasswordField passwordField) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
    
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Both username and password are required.");
            return;
        }
    
        if (checkCredentials(username, password)) {
            updateStatusToOnline(username);
            // This line will work correctly as it calls loadMainScreen() in Main and passes the primaryStage
            mainApp.loadMainScreen(username);  // Load the main screen after successful login
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password.");
        }
    }
    private void updateStatusToOnline(String username) {
        String updateSql = "UPDATE client_profile SET status = 'online' WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status to online.");
        }
    }
    private void handleSignIn(TextField usernameField, PasswordField passwordField) {
        // Similar logic for handling sign-in
    }

    private boolean checkCredentials(String username, String password) {
        String sql = "SELECT * FROM client_login WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
