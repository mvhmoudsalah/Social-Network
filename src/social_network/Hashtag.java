package social_network;

import java.util.Date;

public class Hashtag {
    private int id;
    private final String tag;
    private final Date createdAt;

    public Hashtag(int id, String tag, Date createdAt) {
        this.id = id;
        this.tag = normalize(tag);
        this.createdAt = createdAt;
    }

    /** Stored lowercase without '#', max 100 characters (same as the database). */
    public static String normalize(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Hashtag cannot be empty.");
        }
        String t = raw.trim();
        if (t.startsWith("#")) {
            t = t.substring(1);
        }
        t = t.toLowerCase();
        if (t.isEmpty() || t.length() > 100) {
            throw new IllegalArgumentException("Hashtag must be 1 to 100 characters.");
        }
        return t;
    }

    public int getId() { return id; }

    void setId(int id) { this.id = id; }

    public String getTag() { return tag; }

    public Date getCreatedAt() { return createdAt; }

    @Override
    public String toString() { return "#" + tag; }
}