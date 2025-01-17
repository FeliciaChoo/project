package com.csc3202.lab.project;
// ChatMessage.java
import java.io.Serializable;
import java.time.LocalDateTime;

public class chatMessage implements Serializable {
    private String sender;
    private String content;
    private String recipient;
    private LocalDateTime timestamp;
    private MessageType type;

    public enum MessageType {
        PUBLIC, PRIVATE, USER_JOIN, USER_LEAVE, STATUS, SYSTEM
    }

    public chatMessage(String sender, String content, String recipient, MessageType type) {
        this.sender = sender;
        this.content = content;
        this.recipient = recipient;
        this.timestamp = LocalDateTime.now();
        this.type = type;
    }

    // Getters
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getRecipient() { return recipient; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public MessageType getType() { return type; }
}
