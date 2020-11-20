package com.ms.social.model;

public class Comment {
    String userId, messageText;

    public Comment (){}

    public Comment(String userId, String messageText) {
        this.userId = userId;
        this.messageText = messageText;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
