package social_network.model.social;

import java.util.Date;

public class BlockedUser {

    private int blockerId;
    private int blockedId;
    private Date createdAt;

    // Constructor
    public BlockedUser(int blockerId, int blockedId) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
        this.createdAt = new Date();
    }

    // Getters
    public int getBlockerId() {
        return blockerId;
    }

    public int getBlockedId() {
        return blockedId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}