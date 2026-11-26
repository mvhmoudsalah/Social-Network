package social_network.model.social;

import java.util.Date;

public class Follow {

    private int followerId;
    private int followingId;
    private Date createdAt;

    // Constructor
    public Follow(int followerId, int followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
        this.createdAt = new Date();
    }

    // Getters
    public int getFollowerId() {
        return followerId;
    }

    public int getFollowingId() {
        return followingId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}