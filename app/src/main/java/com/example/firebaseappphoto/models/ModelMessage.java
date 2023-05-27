package com.example.firebaseappphoto.models;

import com.google.firebase.database.PropertyName;

public class ModelMessage {
    private String message, recipient, sender, timestamp, type;
    private boolean isSeen;

    public ModelMessage() {
    }

    public ModelMessage(String message, String recipient, String sender, String timestamp, String type, boolean isSeen) {
        this.message = message;
        this.recipient = recipient;
        this.sender = sender;
        this.timestamp = timestamp;
        this.type = type;
        this.isSeen = isSeen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("isSeen")
    public boolean isSeen() {
        return isSeen;
    }
    @PropertyName("isSeen")
    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
