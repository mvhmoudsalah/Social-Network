package social_network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

public class Message {
    private int id;
    private int conversationId;
    private int senderId;
    private String content;
    private Date createdAt;
    private boolean isRead;

    // new message (not saved yet)
    public Message(int conversationId, int senderId, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = new Date();
        this.isRead = false;
    }

    // message loaded from the database
    public Message(int id, int conversationId, int senderId, String content, Date createdAt, boolean isRead) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public void save() throws SQLException {
        String sql = "INSERT INTO message (conversation_id, sender_id, content, created_at, is_read) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, senderId);
            ps.setString(3, content);
            ps.setTimestamp(4, new Timestamp(createdAt.getTime()));
            ps.setBoolean(5, isRead);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        }
    }

    public void markAsRead() throws SQLException {
        String sql = "UPDATE message SET is_read = TRUE WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        this.isRead = true;
    }

    public static Message findById(int id) throws SQLException {
        String sql = "SELECT * FROM message WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Message(
                            rs.getInt("id"),
                            rs.getInt("conversation_id"),
                            rs.getInt("sender_id"),
                            rs.getString("content"),
                            rs.getTimestamp("created_at"),
                            rs.getBoolean("is_read"));
                }
                return null;
            }
        }
    }

    public int getId() { return id; }
    public int getConversationId() { return conversationId; }
    public int getSenderId() { return senderId; }
    public String getContent() { return content; }
    public Date getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/social_network", "root", "");
    }
}