package social_network;

import java.util.Date;

public class Like {
    private final int userId;
    private final int postId;
    private final Date createdAt;

    public Like(int userId, int postId, Date createdAt) {
        this.userId = userId;
        this.postId = postId;
        this.createdAt = createdAt;
    }

    public int getUserId() { return userId; }

    public int getPostId() { return postId; }

    public Date getCreatedAt() { return createdAt; }
}