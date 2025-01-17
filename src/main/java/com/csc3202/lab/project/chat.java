package com.csc3202.lab.project;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class chat extends Application {

    private TextField messageInput;

    @Override
    public void start(Stage primaryStage) {
        // Main layout
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #FFB6C1;");

        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.setPrefWidth(150);

        // Top bar layout
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #FFE4E1;");

        // Circle icon (Avatar)
        Button circleIcon = new Button();
        circleIcon.setShape(new javafx.scene.shape.Circle(15));
        circleIcon.setPrefSize(30, 30);
        circleIcon.setStyle("-fx-background-color: #FFFFFF;");

        // WhatsApp Title
        Label titleLabel = new Label("WhatsApp");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #000000;");
        titleLabel.setPadding(new Insets(0, 10, 0, 0));

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
        exploreIcon.setOnAction(event -> searchInput.setVisible(!searchInput.isVisible()));

        rightIcons.getChildren().addAll(searchInput, exploreIcon);
        HBox.setHgrow(rightIcons, Priority.ALWAYS);
        topBar.getChildren().addAll(circleIcon, titleLabel, rightIcons);

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
        emojiButton.setOnAction(event -> emojiPicker.show(primaryStage));

        Label cameraLabel = new Label("📷");
        cameraLabel.setStyle("-fx-font-size: 16px;");

        // Modify linkButton to make it look like the camera label
Button linkButton = new Button("🔗");
linkButton.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-border-width: 0;");


        Button sendButton = new Button("⬆");
        sendButton.setStyle("-fx-font-size: 16px;");

        inputArea.getChildren().addAll(emojiButton, messageInput, cameraLabel, linkButton, sendButton);

        sendButton.setOnAction(event -> sendMessage(chatArea, messageInput));
        messageInput.setOnAction(event -> sendMessage(chatArea, messageInput));

        // Bottom labels and buttons
        HBox bottomLabels = new HBox();
        bottomLabels.setPadding(new Insets(10));
        bottomLabels.setSpacing(20);
        bottomLabels.setAlignment(Pos.CENTER);

        Button friendListButton = new Button("Friend List");
        friendListButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-padding: 10; -fx-border-radius: 5;");

        Button settingsButton = new Button("Settings");
        settingsButton.setStyle("-fx-background-color: #FF69B4; -fx-text-fill: white; -fx-padding: 10; -fx-border-radius: 5;");

        bottomLabels.getChildren().addAll(settingsButton,friendListButton);

        // Add components to the root layout
        root.getChildren().addAll(topBar, chatScroll, inputArea, bottomLabels);
        VBox.setVgrow(chatScroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 362, 616);
        primaryStage.setTitle("Chat Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

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

    public static void main(String[] args) {
        launch(args);
    }
}
