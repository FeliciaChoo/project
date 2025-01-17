package com.csc3202.lab.project;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileSettingsController {

    @FXML
    private Circle profileImageCircle;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private ChoiceBox<String> statusChoiceBox;
    @FXML
    private Button uploadImageButton;
    @FXML
    private Button saveChangesButton;
    @FXML
    private Button saveProfileButton; // New button for saving profile (first-time save)

    private final FileChooser fileChooser = new FileChooser();
    private Connection connection;
    private File profileImageFile;

    // Set the connection from the clientPlatform class
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // Initialize method to set up the file chooser and button actions
    public void initialize() {
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        uploadImageButton.setOnAction(e -> handleUploadImage());
        saveChangesButton.setOnAction(e -> handleSaveChanges());
        saveProfileButton.setOnAction(e -> handleSaveProfile());  // Action for save profile button
    }

    // Handle uploading the profile image
    private void handleUploadImage() {
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                // Store the file for later use
                profileImageFile = selectedFile;

                // Convert the file to an Image and set it to the profile image circle
                Image image = new Image(new FileInputStream(selectedFile));
                profileImageCircle.setFill(new javafx.scene.paint.ImagePattern(image));
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("Error loading image: " + ex.getMessage());
            }
        }
    }

    private void handleSaveChanges() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String status = statusChoiceBox.getValue();
    
        // Validate that all fields are filled out
        if (username.isEmpty() || email.isEmpty() || status == null || profileImageFile == null) {
            System.out.println("Please fill all fields and upload a profile image.");
            return;
        }
    
        // Optionally, you can validate the email format
        if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            System.out.println("Invalid email format.");
            return;
        }
    
        // Debug output for validation
        System.out.println("Saving profile for: " + username + ", " + email + ", " + status);
    
        // Prepare the SQL statement to update the user's profile
        String sql = "UPDATE client_profile SET username = ?, email = ?, status = ?, profile_image_path = ? WHERE user_id = ?";
    
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set the new values for the prepared statement
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, status);
            stmt.setString(4, profileImageFile.getAbsolutePath());  // Save the file path of the profile image
            stmt.setInt(5, getUserId());  // Retrieve the correct user ID of the logged-in user
    
            // Execute the update statement
            int rowsUpdated = stmt.executeUpdate();
    
            if (rowsUpdated > 0) {
                System.out.println("Profile updated successfully.");
            } else {
                System.out.println("Failed to update profile.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error saving profile changes: " + e.getMessage());
        }
    }
    
    // This is a placeholder method, assuming you have a way to get the logged-in user's ID
    private int getUserId() {
        // For example, you could retrieve the user's ID from the session or stored somewhere
        return 1;  // Dummy value, replace with actual logic to get user_id
    }

    // Handle saving a new profile (first-time save)
    private void handleSaveProfile() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String status = statusChoiceBox.getValue();
    
        // Validate that all fields are filled out
        if (username.isEmpty() || email.isEmpty() || status == null || profileImageFile == null) {
            System.out.println("Please fill all fields and upload a profile image.");
            return;
        }
    
        // Optionally, you can validate the email format
        if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            System.out.println("Invalid email format.");
            return;
        }
    
        // Debug output for validation
        System.out.println("Saving new profile for: " + username + ", " + email + ", " + status);
    
        // Prepare the SQL statement to insert the user's profile
        String sql = "INSERT INTO client_profile (username, email, status, profile_image_path) VALUES (?, ?, ?, ?)";
    
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set the new values for the prepared statement
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, status);
            stmt.setString(4, profileImageFile.getAbsolutePath());  // Save the file path of the profile image
    
            // Execute the insert statement
            int rowsInserted = stmt.executeUpdate();
    
            if (rowsInserted > 0) {
                System.out.println("Profile created successfully.");
            } else {
                System.out.println("Failed to create profile.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error creating profile: " + e.getMessage());
        }
    }
}
