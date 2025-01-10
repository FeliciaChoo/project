package com.csc3202.lab.project;

//ClientPlatform.java
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class clientPlatform extends Application {
    private chatService chatService;
    private String username;
    private TextArea chatArea;
    private TextField messageField;
    private ListView<String> userList;
    private clientNotifierImpl clientNotifier;
    private clientProfile myProfile;
    private Label messageTypeLabel;
    private ToggleButton privateMessageToggle;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Login dialog
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Chat Login");
        dialog.setHeaderText("Enter your username:");
        dialog.setContentText("Username:");
        dialog.showAndWait().ifPresent(name -> {
            username = name;
            initializeRMIConnection();
            createAndShowGUI(primaryStage);
        });
    }

    private void initializeRMIConnection() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 5000);
            chatService = (chatService) registry.lookup("ChatService");
            clientNotifier = new clientNotifierImpl();
            clientNotifier stub = (clientNotifier) UnicastRemoteObject.exportObject(clientNotifier, 0);
            chatService.registerClient(username, stub);
            // Initialize profile
            myProfile = new clientProfile(username);
            chatService.updateProfile(username, myProfile);
        } catch (Exception e) {
            showError("Connection Error", "Failed to connect to the server: " + e.getMessage());
        }
    }

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

        // Event handlers
        privateMessageToggle.setOnAction(e -> {
            String selectedUser = userList.getSelectionModel().getSelectedItem();
            if (privateMessageToggle.isSelected() && selectedUser != null) {
                messageTypeLabel.setText("To: " + selectedUser);
            } else {
                messageTypeLabel.setText("To: Everyone");
                privateMessageToggle.setSelected(false);
            }
        });

        // User list selection handler
        userList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && privateMessageToggle.isSelected()) {
                messageTypeLabel.setText("To: " + newVal);
            }
        });

        // Layout
        VBox centerBox = new VBox(10);
        centerBox.getChildren().addAll(chatArea, messageBox);
        root.setCenter(centerBox);
        root.setRight(userList);

        // Scene
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Chat Client - " + username);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Handle window closing
        primaryStage.setOnCloseRequest(e -> {
            try {
                chatService.unregisterClient(username);
                Platform.exit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Enter key sends message
        messageField.setOnAction(e -> sendMessage());

        // Profile button action
        profileButton.setOnAction(e -> showProfileDialog());

        // Back button action
        backButton.setOnAction(e -> {
            try {
                chatService.unregisterClient(username);
                primaryStage.close();
                showLoginScreen(new Stage());
            } catch (RemoteException ex) {
                showError("Error", "Failed to logout: " + ex.getMessage());
            }
        });

        // Status combo action
        statusCombo.setOnAction(e -> {
            try {
                chatService.setUserStatus(username, statusCombo.getValue());
            } catch (RemoteException ex) {
                showError("Error", "Failed to update status: " + ex.getMessage());
            }
        });

        // Double click on user list to view profile
        userList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedUser = userList.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    showUserProfile(selectedUser);
                }
            }
        });
    }

    private void sendMessage() {
        try {
            String content = messageField.getText().trim();
            if (!content.isEmpty()) {
                String selectedUser = userList.getSelectionModel().getSelectedItem();
                chatMessage.MessageType type = (!privateMessageToggle.isSelected() || selectedUser == null) ?
                        chatMessage.MessageType.PUBLIC : chatMessage.MessageType.PRIVATE;

                chatMessage message = new chatMessage(username, content, selectedUser, type);

                if (type == chatMessage.MessageType.PUBLIC) {
                    chatService.broadcastMessage(message);
                } else {
                    chatService.sendPrivateMessage(message);
                }
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

    private void showProfileDialog() {
        // Initialize profile if null
        if (myProfile == null) {
            myProfile = new clientProfile(username);
        }

        Dialog<clientProfile> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Edit your profile");

        // Create the custom layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField bioField = new TextField(myProfile.getBio());
        TextField avatarField = new TextField(myProfile.getAvatarUrl());

        grid.add(new Label("Bio:"), 0, 0);
        grid.add(bioField, 1, 0);
        grid.add(new Label("Avatar URL:"), 0, 1);
        grid.add(avatarField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                try {
                    myProfile.setBio(bioField.getText());
                    myProfile.setAvatarUrl(avatarField.getText());
                    chatService.updateProfile(username, myProfile);
                    return myProfile;
                } catch (RemoteException ex) {
                    showError("Error", "Failed to update profile: " + ex.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showUserProfile(String username) {
        try {
            clientProfile profile = chatService.getUserProfile(username);
            if (profile != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("User Profile");
                alert.setHeaderText(username + "'s Profile");
                alert.setContentText(
                        "Status: " + profile.getStatus() + "\n" +
                                "Bio: " + profile.getBio()
                );
                alert.showAndWait();
            }
        } catch (RemoteException ex) {
            showError("Error", "Failed to load profile: " + ex.getMessage());
        }
    }

    private void showLoginScreen(Stage stage) {
        // Create new login screen
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Chat Login");
        dialog.setHeaderText("Enter your username:");
        dialog.setContentText("Username:");
        dialog.showAndWait().ifPresent(name -> {
            username = name;
            initializeRMIConnection();
            createAndShowGUI(stage);
        });
    }

    private class clientNotifierImpl implements clientNotifier {  // Rename here
        @Override
        public void receiveMessage(chatMessage message) throws RemoteException {
            Platform.runLater(() -> {
                String formattedMessage = formatMessage(message);
                chatArea.appendText(formattedMessage + "\n");
            });
        }
    
        @Override
        public void updateUserList(List<String> users) throws RemoteException {
            Platform.runLater(() -> {
                userList.getItems().clear();
                userList.getItems().addAll(users);
            });
        }
    
        private String formatMessage(chatMessage message) {
            String timestamp = message.getTimestamp().toString();
            switch (message.getType()) {
                case PRIVATE:
                    return String.format("[%s] 📱 PRIVATE %s ➜ %s: %s",
                            timestamp, message.getSender(), message.getRecipient(), message.getContent());
                case USER_JOIN:
                    return String.format("[%s] 👋 %s", timestamp, message.getContent());
                case USER_LEAVE:
                    return String.format("[%s] 🚶 %s", timestamp, message.getContent());
                case STATUS:
                    return String.format("[%s] 🔄 %s", timestamp, message.getContent());
                default:
                    return String.format("[%s] 📢 %s: %s",
                            timestamp, message.getSender(), message.getContent());
            }
        }
    }
}    