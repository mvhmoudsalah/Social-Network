package social_network.model.content;

import java.util.Date;

public class Share {
    private int id;
    private final int userId;
    private final int postId;
    private final Date createdAt;

    public Share(int id, int userId, int postId, Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }

    /** Used after an insert, when the database gives the new id. */
    void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }

    public int getPostId() { return postId; }

    public Date getCreatedAt() { return createdAt; }
}