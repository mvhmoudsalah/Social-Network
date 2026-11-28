package social_network.model.user;

import java.sql.*;
import java.util.Date;
import social_network.database.ConnectDB;
import social_network.model.enums.PostVisibility;

/** OOP + DB in one class. Table: USER_SETTINGS (one row per user, PK = user_id) */
public class UserSettings {

    // ---------- fields (defaults match the DB defaults) ----------
    private int userId;
    private boolean isPrivate = false;
    private PostVisibility postVisibility = PostVisibility.PUBLIC;
    private String whoCanMessage = "everyone";   // everyone | friends | nobody
    private boolean notifyEnabled = true;
    private String language = "en";
    private String theme = "system";             // light | dark | system
    private Date updatedAt;

    // ---------- constructors ----------
    public UserSettings(int userId) { this.userId = userId; }

    // ---------- OOP methods ----------
    /** Copies every setting from another object (does not touch the DB; call save() after). */
    public void update(UserSettings s) {
        this.isPrivate = s.isPrivate;
        this.postVisibility = s.postVisibility;
        this.whoCanMessage = s.whoCanMessage;
        this.notifyEnabled = s.notifyEnabled;
        this.language = s.language;
        this.theme = s.theme;
    }

    // ---------- DB: instance method ----------
    /** Insert or update in one statement (MySQL upsert). */
    public void save() throws SQLException {
        String sql = "INSERT INTO user_settings "
                   + "(user_id, is_private, post_visibility, who_can_message, notify_enabled, language, theme) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE is_private = VALUES(is_private), "
                   + "post_visibility = VALUES(post_visibility), who_can_message = VALUES(who_can_message), "
                   + "notify_enabled = VALUES(notify_enabled), language = VALUES(language), "
                   + "theme = VALUES(theme), updated_at = CURRENT_TIMESTAMP";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setBoolean(2, isPrivate);
            ps.setString(3, postVisibility.toDb());
            ps.setString(4, whoCanMessage);
            ps.setBoolean(5, notifyEnabled);
            ps.setString(6, language);
            ps.setString(7, theme);
            ps.executeUpdate();
        }
    }

    // ---------- DB: static methods ----------
    /** Call right after a user registers: inserts the default row if none exists. */
    public static void createDefault(int userId) throws SQLException {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO user_settings (user_id) VALUES (?)")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /** Returns the user's settings, or null if there is no row. */
    public static UserSettings findByUserId(int userId) throws SQLException {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM user_settings WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                UserSettings s = new UserSettings(userId);
                s.isPrivate = rs.getBoolean("is_private");
                s.postVisibility = PostVisibility.fromDb(rs.getString("post_visibility"));
                s.whoCanMessage = rs.getString("who_can_message");
                s.notifyEnabled = rs.getBoolean("notify_enabled");
                s.language = rs.getString("language");
                s.theme = rs.getString("theme");
                s.updatedAt = rs.getTimestamp("updated_at");
                return s;
            }
        }
    }

    // ---------- getters / setters ----------
    public int getUserId() { return userId; }
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean p) { this.isPrivate = p; }
    public PostVisibility getPostVisibility() { return postVisibility; }
    public void setPostVisibility(PostVisibility v) { this.postVisibility = v; }
    public String getWhoCanMessage() { return whoCanMessage; }
    public void setWhoCanMessage(String w) {
        if (!w.matches("everyone|friends|nobody")) throw new IllegalArgumentException("Bad value: " + w);
        this.whoCanMessage = w;
    }
    public boolean isNotifyEnabled() { return notifyEnabled; }
    public void setNotifyEnabled(boolean n) { this.notifyEnabled = n; }
    public String getLanguage() { return language; }
    public void setLanguage(String l) { this.language = l; }
    public String getTheme() { return theme; }
    public void setTheme(String t) {
        if (!t.matches("light|dark|system")) throw new IllegalArgumentException("Bad value: " + t);
        this.theme = t;
    }
    public Date getUpdatedAt() { return updatedAt; }
}