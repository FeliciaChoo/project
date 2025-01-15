package com.csc3202.lab.project;
import com.example.ChatProject.common.chatMessage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientPlatform extends Application {
    private ChatClientSocket chatClientSocket;
    private String username;
    private TextArea chatArea;
    private TextField messageField;
    private ListView<String> userList;
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
            initializeSocketConnection("localhost", 9000); // Initialize the socket connection
            createAndShowGUI(primaryStage); // Create and display the GUI
        });
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

    private void createAndShowGUI(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Initialize components
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

        // Message input area
        Label messageTypeLabel = new Label("To: Everyone");
        privateMessageToggle = new ToggleButton("Private Message");
        Button sendButton = new Button("Send");

        // Layout for message input area
        BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(messageTypeLabel);
        bottomPane.setRight(privateMessageToggle);
        bottomPane.setPadding(new Insets(5));

        VBox inputBox = new VBox(5);
        inputBox.getChildren().addAll(messageField, sendButton);
        root.setCenter(chatArea);
        root.setBottom(inputBox);
        root.setRight(userList);

        // Message sending logic
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());

        // Logic for closing the application
        primaryStage.setOnCloseRequest(e -> {
            try {
                chatClientSocket.sendMessage(new chatMessage(username, "has disconnected.", null, chatMessage.MessageType.USER_LEAVE));
                Platform.exit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Chat Client - " + username);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendMessage() {
        try {
            String content = messageField.getText().trim();
            if (!content.isEmpty()) {
                String selectedUser = userList.getSelectionModel().getSelectedItem();
                chatMessage.MessageType type = (!privateMessageToggle.isSelected() || selectedUser == null)
                        ? chatMessage.MessageType.PUBLIC
                        : chatMessage.MessageType.PRIVATE;

                chatMessage message = new chatMessage(username, content, selectedUser, type);
                chatClientSocket.sendMessage(message);

                // Update the local UI
                Platform.runLater(() -> chatArea.appendText("[" + message.getTimestamp() + "] You: " + content + "\n"));
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
}
