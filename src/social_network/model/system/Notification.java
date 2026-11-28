package social_network.model.system;



import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import social_network.database.ConnectDB;
import social_network.model.enums.NotificationType;

/** OOP + DB in one class. Table: NOTIFICATIONS */
public class Notification {

    // ---------- fields ----------
    private int id;                 // 0 = not saved yet
    private int userId;             // recipient
    private int actorId;            // who triggered it
    private NotificationType type;
    private Integer postId;         // nullable
    private Integer commentId;      // nullable
    private boolean isRead;
    private Date createdAt;

    // ---------- constructors ----------
    public Notification(int userId, int actorId, NotificationType type, Integer postId, Integer commentId) {
        this.userId = userId;
        this.actorId = actorId;
        this.type = type;
        this.postId = postId;
        this.commentId = commentId;
    }

    private Notification(ResultSet rs) throws SQLException {
        this.id = rs.getInt("id");
        this.userId = rs.getInt("user_id");
        this.actorId = rs.getInt("actor_id");
        this.type = NotificationType.fromDb(rs.getString("type"));
        this.postId = rs.getObject("post_id", Integer.class);
        this.commentId = rs.getObject("comment_id", Integer.class);
        this.isRead = rs.getBoolean("is_read");
        this.createdAt = rs.getTimestamp("created_at");
    }

    // ---------- DB: instance methods ----------
    /** INSERT a new notification (id == 0). Existing ones only change through markAsRead(). */
    public void save() throws SQLException {
        if (id != 0) return;
        String sql = "INSERT INTO notifications (user_id, actor_id, type, post_id, comment_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, actorId);
            ps.setString(3, type.toDb());
            if (postId == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, postId);
            if (commentId == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, commentId);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) id = k.getInt(1); }
        }
    }

    /** Marks this object as read and updates the row if it is already saved. */
    public void markAsRead() throws SQLException {
        this.isRead = true;
        if (id == 0) return;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE notifications SET is_read = TRUE WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public boolean delete() throws SQLException {
        if (id == 0) return false;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM notifications WHERE id = ?")) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) id = 0;
            return ok;
        }
    }

    // ---------- DB: static finders ----------
    /** Newest first (uses idx_notif_user). */
    public static List<Notification> findByUser(int userId, boolean unreadOnly) throws SQLException {
        String sql = "SELECT * FROM notifications WHERE user_id = ? "
                   + (unreadOnly ? "AND is_read = FALSE " : "") + "ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(new Notification(rs)); }
        }
        return list;
    }

    public static int countUnread(int userId) throws SQLException {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    public static int markAllAsRead(int userId) throws SQLException {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE")) {
            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }

    // ---------- getters ----------
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getActorId() { return actorId; }
    public NotificationType getType() { return type; }
    public Integer getPostId() { return postId; }
    public Integer getCommentId() { return commentId; }
    public boolean isRead() { return isRead; }
    public Date getCreatedAt() { return createdAt; }
}