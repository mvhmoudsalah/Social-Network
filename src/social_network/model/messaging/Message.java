package social_network.model.messaging;

import java.util.Date;

public class Message {

    private int id;
    private int conversationId;
    private int senderId;
    private String content;
    private Date createdAt;
    private boolean isRead;

    // Constructor
    public Message(int id, int conversationId, int senderId, String content) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = new Date();
        this.isRead = false;
    }

    // Mark message as read
    public void markAsRead() {
        isRead = true;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return isRead;
    }
}