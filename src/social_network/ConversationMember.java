package social_network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class ConversationMember {
    private int conversationId;
    private int userId;
    private Date joinedAt;

    // new member (not saved yet)
    public ConversationMember(int conversationId, int userId) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.joinedAt = new Date();
    }

    // member loaded from the database
    public ConversationMember(int conversationId, int userId, Date joinedAt) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public void save() throws SQLException {
        String sql = "INSERT INTO conversation_member (conversation_id, user_id, joined_at) VALUES (?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, conversationId);
            ps.setInt(2, userId);
            ps.setTimestamp(3, new Timestamp(joinedAt.getTime()));
            ps.executeUpdate();
        }
    }

    public int getConversationId() { return conversationId; }
    public int getUserId() { return userId; }
    public Date getJoinedAt() { return joinedAt; }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/social_network", "root", "");
    }
}