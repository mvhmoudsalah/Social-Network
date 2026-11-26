package social_network.model.messaging;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class Conversation {

    private int id;
    private Date createdAt;
    private List<Message> messages;
    private List<Integer> members;

    // Constructor
    public Conversation(int id) {
        this.id = id;
        this.createdAt = new Date();
        this.messages = new ArrayList<>();
        this.members = new ArrayList<>();
    }

    // Add member to conversation
    public void addMember(int userId) {
        members.add(userId);
    }

    // Get conversation messages
    public List<Message> getMessages() {
        return messages;
    }

    // Getters
    public int getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}