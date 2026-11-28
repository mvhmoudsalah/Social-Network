package social_network.model.content;



import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import social_network.database.ConnectDB;

/** OOP + DB in one class. Table: COMMENTS (id, post_id, user_id, content, created_at) */
public class Comment {

    // ---------- fields ----------
    private int id;                 // 0 = not saved yet
    private int postId;
    private int authorId;
    private String content;
    private Date createdAt;

    // ---------- constructors ----------
    public Comment(int postId, int authorId, String content) {
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
    }

    private Comment(ResultSet rs) throws SQLException {
        this.id = rs.getInt("id");
        this.postId = rs.getInt("post_id");
        this.authorId = rs.getInt("user_id");
        this.content = rs.getString("content");
        this.createdAt = rs.getTimestamp("created_at");
    }

    // ---------- OOP methods ----------
    public void edit(String newContent) {
        if (newContent == null || newContent.isBlank())
            throw new IllegalArgumentException("Comment cannot be empty");
        this.content = newContent;
    }

    // ---------- DB: instance methods ----------
    /** INSERT if new (id == 0), otherwise UPDATE the content. */
    public void save() throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            if (id == 0) {
                String sql = "INSERT INTO comments (post_id, user_id, content) VALUES (?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, postId);
                    ps.setInt(2, authorId);
                    ps.setString(3, content);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) id = k.getInt(1); }
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement("UPDATE comments SET content = ? WHERE id = ?")) {
                    ps.setString(1, content);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }
        }
    }

    /** Related notifications are removed by ON DELETE CASCADE. */
    public boolean delete() throws SQLException {
        if (id == 0) return false;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM comments WHERE id = ?")) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) id = 0;
            return ok;
        }
    }

    // ---------- DB: static finders ----------
    public static Comment findById(int id) throws SQLException {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM comments WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? new Comment(rs) : null; }
        }
    }

    /** Oldest first (uses idx_comments_post). */
    public static List<Comment> findByPost(int postId) throws SQLException {
        List<Comment> list = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM comments WHERE post_id = ? ORDER BY created_at ASC")) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(new Comment(rs)); }
        }
        return list;
    }

    public static int countByPost(int postId) throws SQLException {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM comments WHERE post_id = ?")) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    // ---------- getters ----------
    public int getId() { return id; }
    public int getPostId() { return postId; }
    public int getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public Date getCreatedAt() { return createdAt; }
}
