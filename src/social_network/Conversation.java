package social_network;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

public class Conversation {
    private int id;
    private Date createdAt;

    // new conversation (not saved yet)
    public Conversation() {
        this.createdAt = new Date();
    }

    // conversation loaded from the database
    public Conversation(int id, Date createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public void save() throws SQLException {
        String sql = "INSERT INTO conversation (created_at) VALUES (?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, new Timestamp(createdAt.getTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        }
    }

    public void addMember(int userId) throws SQLException {
        new ConversationMember(id, userId).save();
    }

    // prints the conversation's messages straight from the database
    public void getMessages() throws SQLException {
        String sql = "SELECT sender_id, content, created_at FROM message WHERE conversation_id = ? ORDER BY created_at";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("[" + rs.getTimestamp("created_at") + "] "
                            + rs.getInt("sender_id") + ": " + rs.getString("content"));
                }
            }
        }
    }

    public static Conversation findById(int id) throws SQLException {
        String sql = "SELECT * FROM conversation WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Conversation(rs.getInt("id"), rs.getTimestamp("created_at"));
                }
                return null;
            }
        }
    }

    public int getId() { return id; }
    public Date getCreatedAt() { return createdAt; }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/social_network", "root", "");
    }
}