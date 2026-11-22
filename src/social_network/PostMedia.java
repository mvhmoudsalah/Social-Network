package social_network;

public class PostMedia {
    private int id;
    private final int postId;
    private final String mediaUrl;
    private final MediaType mediaType;

    public PostMedia(int id, int postId, String mediaUrl, MediaType mediaType) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            throw new IllegalArgumentException("Media URL cannot be empty.");
        }
        if (mediaUrl.length() > 500) {
            throw new IllegalArgumentException("Media URL is longer than 500 characters.");
        }
        if (mediaType == null) {
            throw new IllegalArgumentException("Media type is required.");
        }
        this.id = id;
        this.postId = postId;
        this.mediaUrl = mediaUrl.trim();
        this.mediaType = mediaType;
    }

    public int getId() { return id; }

    /** Used after an insert, when the database gives the new id. */
    void setId(int id) { this.id = id; }

    public int getPostId() { return postId; }

    public String getMediaUrl() { return mediaUrl; }

    public MediaType getMediaType() { return mediaType; }
}