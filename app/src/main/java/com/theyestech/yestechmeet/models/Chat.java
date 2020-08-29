package com.theyestech.yestechmeet.models;

import java.util.Date;

public class Chat {

    public String id;
    public String message;
    public Date messageDateCreated;
    public String receiverId;
    public String senderId;
    private boolean isseen;

    public Chat(String id, String message, Date messageDateCreated, String receiverId, String senderId, boolean isseen) {
        this.id = id;
        this.message = message;
        this.messageDateCreated = messageDateCreated;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.isseen = isseen;
    }

    public Chat() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getMessageDateCreated() {
        return messageDateCreated;
    }

    public void setMessageDateCreated(Date messageDateCreated) {
        this.messageDateCreated = messageDateCreated;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}
