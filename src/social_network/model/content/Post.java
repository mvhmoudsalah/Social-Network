package social_network.model.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import social_network.database.ConnectDB;
import social_network.model.enums.PostVisibility;

public class Post {

    
    private int id;
    private final int authorId;
    private String content;
    private PostVisibility visibility;
    private final Date createdAt;
    private Date updatedAt;
    private List<String> hashtags = new ArrayList<>();

    public Post(int id, int authorId, String content, PostVisibility visibility,
                Date createdAt, Date updatedAt) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.visibility = visibility;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ================= Behaviour from the UML =================

    /** Changes the content, refreshes updatedAt, rebuilds hashtags and saves. */
    public void edit(String newContent) throws SQLException {
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("Post content cannot be empty.");
        }
        this.content = newContent;
        this.updatedAt = new Date();
        save();
    }

    /** PUBLIC = everyone, FRIENDS = author and friends, PRIVATE = author only. */
    public boolean canView(int viewerId) throws SQLException {
        if (viewerId == authorId) {
            return true;
        }
        boolean friends = visibility == PostVisibility.FRIENDS
                && areFriends(authorId, viewerId);
        return canView(viewerId, friends);
    }

    /** Same rule without the database (easy to test, can use SocialGraph later). */
    public boolean canView(int viewerId, boolean viewerIsFriend) {
        if (viewerId == authorId) {
            return true;
        }
        switch (visibility) {
            case PUBLIC:  return true;
            case FRIENDS: return viewerIsFriend;
            default:      return false;
        }
    }

    public int getLikeCount() throws SQLException {
        return count("SELECT COUNT(*) FROM likes WHERE post_id = ?", id);
    }

    public int getCommentCount() throws SQLException {
        return count("SELECT COUNT(*) FROM comments WHERE post_id = ?", id);
    }

    // ================= Database: static operations =================

    /** Creates a post (and its hashtags) and returns it. */
    public static Post create(int authorId, String content, PostVisibility visibility)
            throws SQLException {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Post content cannot be empty.");
        }
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                int newId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO posts (user_id, content, visibility) VALUES (?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, authorId);
                    ps.setString(2, content);
                    ps.setString(3, visibility.toDb());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Insert did not return an id.");
                        }
                        newId = keys.getInt(1);
                    }
                }
                saveHashtags(c, newId, content);
                c.commit();
                return findById(c, newId);
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Returns the post, or null if it does not exist. No visibility check. */
    public static Post findById(int id) throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            return findById(c, id);
        }
    }

    /** A user's posts, newest first, only those the viewer may see. */
    public static List<Post> findByUser(int authorId, int viewerId) throws SQLException {
        String sql = SELECT_POST + "WHERE p.user_id = ? AND " + VISIBLE_TO_VIEWER
                + " ORDER BY p.created_at DESC";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.setInt(2, viewerId);
            ps.setInt(3, viewerId);
            ps.setInt(4, viewerId);
            return readPosts(c, ps);
        }
    }

    /** Posts with a hashtag (with or without #), newest first, visible to the viewer. */
    public static List<Post> searchByHashtag(String tag, int viewerId) throws SQLException {
        String clean = tag.startsWith("#") ? tag.substring(1) : tag;
        String sql = SELECT_POST
                + "JOIN post_hashtags ph ON ph.post_id = p.id "
                + "JOIN hashtags h ON h.id = ph.hashtag_id "
                + "WHERE h.tag = ? AND " + VISIBLE_TO_VIEWER
                + " ORDER BY p.created_at DESC";
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, clean.toLowerCase());
            ps.setInt(2, viewerId);
            ps.setInt(3, viewerId);
            ps.setInt(4, viewerId);
            return readPosts(c, ps);
        }
    }

    /** Deletes the post. Comments, likes, etc. go with it (ON DELETE CASCADE). */
    public static boolean delete(int postId) throws SQLException {
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM posts WHERE id = ?")) {
            ps.setInt(1, postId);
            return ps.executeUpdate() > 0;
        }
    }

    /** friendships stores each edge once with user_a < user_b. */
    public static boolean areFriends(int a, int b) throws SQLException {
        if (a == b) {
            return false;
        }
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM friendships WHERE user_a = ? AND user_b = ?")) {
            ps.setInt(1, Math.min(a, b));
            ps.setInt(2, Math.max(a, b));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ================= Database: private helpers =================

    private static final Pattern HASHTAG = Pattern.compile("#([\\p{L}\\p{N}_]{1,100})");

    // Params for this filter: viewer, viewer, viewer.
    private static final String VISIBLE_TO_VIEWER =
            "(p.visibility = 'public' OR p.user_id = ? OR "
            + "(p.visibility = 'friends' AND EXISTS (SELECT 1 FROM friendships f "
            + "WHERE f.user_a = LEAST(p.user_id, ?) AND f.user_b = GREATEST(p.user_id, ?))))";

    private static final String SELECT_POST =
            "SELECT p.id, p.user_id, p.content, p.visibility, p.created_at, p.updated_at "
            + "FROM posts p ";

    

    /** Saves content, visibility and updated_at, and rebuilds the hashtag links. */
    private void save() throws SQLException {
        try (Connection c = ConnectDB.getConnection()) {
            c.setAutoCommit(false);
            try {
                int rows;
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE posts SET content = ?, visibility = ?, updated_at = ? WHERE id = ?")) {
                    ps.setString(1, content);
                    ps.setString(2, visibility.toDb());
                    ps.setTimestamp(3, new Timestamp(updatedAt.getTime()));
                    ps.setInt(4, id);
                    rows = ps.executeUpdate();
                }
                if (rows == 0) {
                    throw new SQLException("Post " + id + " does not exist.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM post_hashtags WHERE post_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                saveHashtags(c, id, content);
                c.commit();
                this.hashtags = extractHashtags(content);
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Lowercase hashtags without '#', in order of first appearance, no duplicates. */
    private static List<String> extractHashtags(String content) {
        Set<String> tags = new LinkedHashSet<>();
        Matcher m = HASHTAG.matcher(content);
        while (m.find()) {
            tags.add(m.group(1).toLowerCase());
        }
        return new ArrayList<>(tags);
    }

    private static void saveHashtags(Connection c, int postId, String content)
            throws SQLException {
        for (String tag : extractHashtags(content)) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT IGNORE INTO hashtags (tag) VALUES (?)")) {
                ps.setString(1, tag);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT IGNORE INTO post_hashtags (post_id, hashtag_id) "
                    + "SELECT ?, id FROM hashtags WHERE tag = ?")) {
                ps.setInt(1, postId);
                ps.setString(2, tag);
                ps.executeUpdate();
            }
        }
    }

    private static int count(String sql, int id) throws SQLException {
        try (Connection c = ConnectDB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static Post findById(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT_POST + "WHERE p.id = ?")) {
            ps.setInt(1, id);
            List<Post> list = readPosts(c, ps);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    private static List<Post> readPosts(Connection c, PreparedStatement ps) throws SQLException {
        List<Post> posts = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                posts.add(new Post(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("content"),
                        PostVisibility.fromDb(rs.getString("visibility")),
                        new Date(rs.getTimestamp("created_at").getTime()),
                        new Date(rs.getTimestamp("updated_at").getTime())));
            }
        }
        for (Post p : posts) {
            p.hashtags = loadHashtags(c, p.id);
        }
        return posts;
    }

    private static List<String> loadHashtags(Connection c, int postId) throws SQLException {
        List<String> tags = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT h.tag FROM post_hashtags ph JOIN hashtags h ON h.id = ph.hashtag_id "
                + "WHERE ph.post_id = ? ORDER BY h.tag")) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tags.add(rs.getString(1));
                }
            }
        }
        return tags;
    }

    // ================= Getters / setters =================

    public int getId() { return id; }

    public int getAuthorId() { return authorId; }

    public String getContent() { return content; }

    public PostVisibility getVisibility() { return visibility; }

    public void setVisibility(PostVisibility visibility) { this.visibility = visibility; }

    public Date getCreatedAt() { return createdAt; }

    public Date getUpdatedAt() { return updatedAt; }

    public List<String> getHashtags() { return new ArrayList<>(hashtags); }
}