package com.csc3202.lab.project;
import javafx.scene.shape.Circle;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;


public class Chat {

    private String friendImagePath;
    private TextField messageInput;
    private String friendUsername;
    private VBox root; // This should be the layout for the chat screen.

    // Constructor to pass the friend's username to the chat screen
    public Chat(String friendUsername, String friendImagePath) {
        this.friendUsername = friendUsername;
        this.friendImagePath = friendImagePath;
        this.root = new VBox(); // Initialize root layout
        root.setStyle("-fx-background-color: #FFB6C1;");
        initializeUI(); // Call method to initialize UI components
    }

    // Method to initialize the UI components
    private void initializeUI() {
        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setPrefWidth(150);

        // Top bar layout
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #FFE4E1;");
        // Load the friend's avatar and set it as the fill of the circle
        Circle avatarCircle = loadAvatar(friendImagePath);
   
        // Title with the friend's username
        Label titleLabel = new Label(friendUsername); // Display friend's username
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #000000;");

        // Search and Options Buttons
        HBox rightIcons = new HBox();
        rightIcons.setSpacing(10);
        rightIcons.setAlignment(Pos.CENTER_RIGHT);

        TextField searchInput = new TextField();
        searchInput.setPromptText("Search...");
        searchInput.setPrefWidth(150);
        searchInput.setVisible(false);

        Button exploreIcon = new Button("🔍");
        exploreIcon.setStyle("-fx-background-color: transparent; -fx-font-size: 16px;");

        rightIcons.getChildren().addAll(searchInput, exploreIcon);
        HBox.setHgrow(rightIcons, Priority.ALWAYS);
        topBar.getChildren().addAll(avatarCircle, titleLabel, rightIcons);

        // Chat area (scrollable)
        ScrollPane chatScroll = new ScrollPane();
        VBox chatArea = new VBox();
        chatArea.setSpacing(10);
        chatArea.setPadding(new Insets(10));
        chatScroll.setContent(chatArea);
        chatScroll.setFitToWidth(true);
        chatScroll.setStyle("-fx-background-color: transparent;");

        // Bottom input area
        HBox inputArea = new HBox();
        inputArea.setSpacing(10);
        inputArea.setPadding(new Insets(10));
        inputArea.setAlignment(Pos.CENTER);

        Button emojiButton = new Button("😊");

        Popup emojiPicker = new Popup();
        emojiPicker.setAutoHide(true);
        GridPane emojiGrid = new GridPane();
        emojiGrid.setPadding(new Insets(10));
        emojiGrid.setHgap(5);
        emojiGrid.setVgap(5);
        String[] emojis = {"😊", "😂", "❤️", "👍", "🎉", "😢", "😡", "🤔", "🙌", "😎"};
        for (int i = 0; i < emojis.length; i++) {
            Button emoji = new Button(emojis[i]);
            emoji.setStyle("-fx-font-size: 18px; -fx-background-color: transparent;");
            emoji.setOnAction(event -> {
                emojiPicker.hide();
                messageInput.setText(messageInput.getText() + emoji.getText());
            });
            emojiGrid.add(emoji, i % 5, i / 5);
        }
        emojiPicker.getContent().add(emojiGrid);
        emojiButton.setOnAction(event -> emojiPicker.show(new Stage())); // Create a new stage for emoji picker

        // Image upload button
        Button cameraButton = new Button("📷");
        cameraButton.setStyle("-fx-font-size: 16px;");
        cameraButton.setOnAction(event -> openFileChooser(chatArea));

        Button sendButton = new Button("⬆");
        sendButton.setStyle("-fx-font-size: 16px;");
        sendButton.setOnAction(event -> sendMessage(chatArea, messageInput));
        messageInput.setOnAction(event -> sendMessage(chatArea, messageInput));
        inputArea.getChildren().addAll(emojiButton, messageInput, cameraButton, sendButton);
        // Add components to the root layout
        root.getChildren().addAll(topBar, chatScroll, inputArea);
        VBox.setVgrow(chatScroll, Priority.ALWAYS);
    }

    // Method to handle image selection
    private void openFileChooser(VBox chatArea) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            // Handle image upload logic
            addMessage(chatArea, "Image: " + selectedFile.getName(), true);
        }
    }

    // Method to send a message
    private void sendMessage(VBox chatArea, TextField messageInput) {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            addMessage(chatArea, message, true);
            messageInput.clear();

            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> simulateFriendReply(chatArea, "Okay, got it!"));
            delay.play();
        }
    }

    // Method to add a message to the chat area
    private void addMessage(VBox chatArea, String message, boolean isUser) {
        HBox messageBox = new HBox();
        messageBox.setSpacing(10);
        messageBox.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        ImageView avatar = new ImageView(new Image(isUser
                ? "https://via.placeholder.com/30/00FF00/FFFFFF?text=U"
                : "https://via.placeholder.com/30/0000FF/FFFFFF?text=F"));
        avatar.setFitWidth(30);
        avatar.setFitHeight(30);

        TextFlow messageBubble = new TextFlow(new Text(message));
        messageBubble.setPadding(new Insets(10));
        messageBubble.setStyle(isUser
                ? "-fx-background-color: #FFB6C1; -fx-background-radius: 10;" // User message background pink
                : "-fx-background-color: #FFFFFF; -fx-background-radius: 10;");
        messageBubble.setMaxWidth(250);

        if (isUser) {
            messageBox.getChildren().addAll(messageBubble, avatar);
        } else {
            messageBox.getChildren().addAll(avatar, messageBubble);
        }

        chatArea.getChildren().add(messageBox);
    }

    // Simulate friend replying to a message with typing effect
    private void simulateFriendReply(VBox chatArea, String reply) {
        HBox friendMessageBox = new HBox();
        friendMessageBox.setSpacing(10);
        friendMessageBox.setAlignment(Pos.CENTER_LEFT);

        ImageView friendAvatar = new ImageView(new Image("https://via.placeholder.com/30/0000FF/FFFFFF?text=F"));
        friendAvatar.setFitWidth(30);
        friendAvatar.setFitHeight(30);

        TextFlow typingBubble = new TextFlow();
        typingBubble.setPadding(new Insets(10));
        typingBubble.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10;");
        typingBubble.setMaxWidth(250);
        friendMessageBox.getChildren().addAll(friendAvatar, typingBubble);
        chatArea.getChildren().add(friendMessageBox);

        Timeline typingEffect = new Timeline();
        StringBuilder displayedText = new StringBuilder();
        for (int i = 0; i < reply.length(); i++) {
            int index = i;
            typingEffect.getKeyFrames().add(new KeyFrame(Duration.millis(50 * i), e -> {
                displayedText.append(reply.charAt(index));
                typingBubble.getChildren().clear();
                typingBubble.getChildren().add(new Text(displayedText.toString()));
            }));
        }
        typingEffect.play();
    }

   // Method to load the friend's avatar as a circular profile image
   private Circle loadAvatar(String imagePath) {
       Circle profilePic = new Circle(15); // Circle with radius 15 for the avatar
   
       if (imagePath != null && !imagePath.isEmpty()) {
           try {
               // Attempt to load the image from the given path
               Image image = new Image("file:" + imagePath);
               profilePic.setFill(new ImagePattern(image)); // Set the image as the fill inside the circle
           } catch (Exception e) {
               // Handle case where image path is invalid or not available
               profilePic.setFill(Color.LIGHTPINK); // Fallback to a light pink color if image is invalid
           }
       } else {
           // Default color if no image path is provided
           profilePic.setFill(Color.LIGHTPINK); // Default fill color
       }
   
       return profilePic;
   }
   

    // Getter for the root layout
    public VBox getRoot() {
        return root;
    }
}
