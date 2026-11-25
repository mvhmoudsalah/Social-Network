package social_network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class GroupMember {
    private int groupId;
    private int userId;
    private GroupRole role;
    private Date joinedAt;

    // new member (not saved yet)
    public GroupMember(int groupId, int userId, GroupRole role) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = new Date();
    }

    // member loaded from the database
    public GroupMember(int groupId, int userId, GroupRole role, Date joinedAt) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public void save() throws SQLException {
        String sql = "INSERT INTO group_member (group_id, user_id, role, joined_at) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setInt(2, userId);
            ps.setString(3, role.name());
            ps.setTimestamp(4, new Timestamp(joinedAt.getTime()));
            ps.executeUpdate();
        }
    }

    public void setRole(GroupRole role) throws SQLException {
        String sql = "UPDATE group_member SET role = ? WHERE group_id = ? AND user_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role.name());
            ps.setInt(2, groupId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
        this.role = role;
    }

    public int getGroupId() { return groupId; }
    public int getUserId() { return userId; }
    public GroupRole getRole() { return role; }
    public Date getJoinedAt() { return joinedAt; }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/social_network", "root", "");
    }
}