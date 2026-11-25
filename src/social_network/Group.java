package social_network;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

public class Group {
    private int id;
    private int createdBy;
    private String name;
    private String description;
    private Date createdAt;

    // new group (not saved yet)
    public Group(int createdBy, String name, String description) {
        this.createdBy = createdBy;
        this.name = name;
        this.description = description;
        this.createdAt = new Date();
    }

    // group loaded from the database
    public Group(int id, int createdBy, String name, String description, Date createdAt) {
        this.id = id;
        this.createdBy = createdBy;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public void save() throws SQLException {
        String sql = "INSERT INTO chat_group (created_by, name, description, created_at) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, createdBy);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setTimestamp(4, new Timestamp(createdAt.getTime()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    this.id = keys.getInt(1);
                }
            }
        }
        // the creator is the group's admin
        new GroupMember(id, createdBy, GroupRole.ADMIN).save();
    }

    public void addMember(int userId) throws SQLException {
        new GroupMember(id, userId, GroupRole.MEMBER).save();
    }

    public void removeMember(int userId) throws SQLException {
        String sql = "DELETE FROM group_member WHERE group_id = ? AND user_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public static Group findById(int id) throws SQLException {
        String sql = "SELECT * FROM chat_group WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Group(
                            rs.getInt("id"),
                            rs.getInt("created_by"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getTimestamp("created_at"));
                }
                return null;
            }
        }
    }

    public int getId() { return id; }
    public int getCreatedBy() { return createdBy; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Date getCreatedAt() { return createdAt; }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/social_network", "root", "");
    }
}