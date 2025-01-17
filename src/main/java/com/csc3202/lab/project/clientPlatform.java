package com.csc3202.lab.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class clientPlatform extends Application {
    private Connection connection;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database connection
        initializeDB();

        try {
            // Load the login FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            BorderPane root = loader.load();

            // Get the controller from the FXMLLoader
            LoginController controller = loader.getController();

            // Inject the database connection into the controller
            controller.setConnection(connection);
            controller.setStage(primaryStage);  // Pass the primaryStage to the controller if needed for scene switching

            // Set up the scene and stage
            Scene scene = new Scene(root);
            primaryStage.setTitle("Chat Application");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading FXML or setting up the stage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeDB() {
        String dbUrl = "jdbc:oracle:thin:@fsktmdbora.upm.edu.my:1521:FSKTM";
        String username = "A222333";
        String password = "222333";

        try {
            // Load Oracle JDBC Driver
            Class.forName("oracle.jdbc.OracleDriver");

            // Establish the connection to the database
            connection = DriverManager.getConnection(dbUrl, username, password);
            System.out.println("Connected to Oracle database server.");
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found. Ensure ojdbc8.jar is in the classpath.");
            e.printStackTrace();
        }
    }

    // Method to load the profile settings scene
    public void loadProfileSettingsScene(Stage primaryStage) {
        try {
            // Load the profile settings FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/profile_settings.fxml"));
            BorderPane root = loader.load();

            // Get the controller from the FXMLLoader and pass the connection if needed
            ProfileSettingsController controller = loader.getController();
            controller.setConnection(connection);

            // Set up the scene and switch to the new stage
            Scene scene = new Scene(root);
            primaryStage.setTitle("Profile Settings");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading profile settings FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Close the database connection when the application stops
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing the database connection: " + e.getMessage());
            }
        }
    }
}
