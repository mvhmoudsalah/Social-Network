package social_network.model.messaging;

import java.util.Date;

public class ConversationMember {

    private int conversationId;
    private int userId;
    private Date joinedAt;

    // Constructor
    public ConversationMember(int conversationId, int userId) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.joinedAt = new Date();
    }

    // Getters
    public int getConversationId() {
        return conversationId;
    }

    public int getUserId() {
        return userId;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }
}