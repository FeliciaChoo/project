package com.csc3202.lab.project;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.shape.Circle;
import java.sql.Connection;

public class ProfileSettingsController {

    @FXML private Circle profileImageCircle;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private ChoiceBox<String> statusChoiceBox;

    private Connection connection;

    // Method to set the database connection
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // Handle the save button logic, such as updating the database
    @FXML
    private void handleSaveChanges() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String status = statusChoiceBox.getValue();
        
        // Database update logic goes here
    }
}
