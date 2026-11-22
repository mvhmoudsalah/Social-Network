package social_network;

import java.util.Date;

public class SavedPost {
    private final int userId;
    private final int postId;
    private final Date savedAt;

    public SavedPost(int userId, int postId, Date savedAt) {
        this.userId = userId;
        this.postId = postId;
        this.savedAt = savedAt;
    }

    public int getUserId() { return userId; }

    public int getPostId() { return postId; }

    public Date getSavedAt() { return savedAt; }
}