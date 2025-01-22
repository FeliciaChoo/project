package com.csc3202.lab.project;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ChoiceBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileController {

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
    private Button saveProfileButton;
    @FXML
    private Button logOutButton;

    private final FileChooser fileChooser = new FileChooser();
    private Connection connection;
    private File profileImageFile;
    private String username;
    private boolean isProfileCreated = false;  // Flag to check if profile exists

    // Set the connection from the clientPlatform class
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setUsername(String username) {
        this.username = username;
        usernameField.setText(username);  // Set the username in the username field

        // Lock the username field if the profile already exists
        checkIfProfileExists();
    }

    // Check if the profile exists in the database
    private void checkIfProfileExists() {
        String sql = "SELECT * FROM client_profile WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                isProfileCreated = true;
                emailField.setText(rs.getString("email"));
                statusChoiceBox.setValue(rs.getString("status"));
                loadProfileImage(rs.getString("profile_image_path"));
                usernameField.setDisable(true);  // Disable username field for editing
                emailField.setDisable(true);     // Disable email field for editing
                uploadImageButton.setDisable(true);  // Disable image upload
                statusChoiceBox.setDisable(true); // Disable status choice box
            } else {
                // Automatically set the status to 'online' if it's the first time
                statusChoiceBox.setValue("online");
                updateStatus("online");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error checking profile: " + e.getMessage());
        }
    }

    // Load the profile image (optional based on your design)
    private void loadProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image(new FileInputStream(imagePath));
                profileImageCircle.setFill(new javafx.scene.paint.ImagePattern(image));
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("Error loading image: " + ex.getMessage());
            }
        }
    }

    // Initialize method to set up the file chooser and button actions
    public void initialize() {
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        uploadImageButton.setOnAction(e -> handleUploadImage());
        saveProfileButton.setOnAction(e -> handleSaveProfile());  // Action for save profile button
        logOutButton.setOnAction(e -> handleLogout()); 
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

    // Handle saving a new profile (first-time save) or updating the profile
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

        if (isProfileCreated) {
            // Update the profile if it already exists
            updateProfile(username, email, status);
        } else {
            // Create a new profile if it doesn't exist
            createNewProfile(username, email, status);
        }
    }

    // Update existing profile
    private void updateProfile(String username, String email, String status) {
        String sql = "UPDATE client_profile SET email = ?, status = ?, profile_image_path = ? WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, status);
            stmt.setString(3, profileImageFile.getAbsolutePath());  // Save the file path of the profile image
            stmt.setString(4, username);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Profile updated successfully.");
            } else {
                System.out.println("Failed to update profile.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating profile: " + e.getMessage());
        }
    }

    // Create a new profile
    private void createNewProfile(String username, String email, String status) {
        String sql = "INSERT INTO client_profile (username, email, status, profile_image_path) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, status);
            stmt.setString(4, profileImageFile.getAbsolutePath());  // Save the file path of the profile image

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

    // Update the user status in the database (online or offline)
    private void updateStatus(String status) {
        String sql = "UPDATE client_profile SET status = ? WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating status: " + e.getMessage());
        }
    }

    // When the user logs out, change their status to "offline"
    public void handleLogout() {
        updateStatus("offline"); // Update user status to "offline"
    // Redirect to the login scene
    try {
        Login login = new Login(new Main()); // Pass your main app reference
        login.start((Stage) logOutButton.getScene().getWindow()); // Use the current stage
    } catch (Exception e) {
        e.printStackTrace();
        System.err.println("Error loading login scene: " + e.getMessage());
    }
    }
}
