package social_network;

import java.util.Date;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;// OOP TO DB
import java.sql.ResultSet;  //DB TO OOP
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;

import org.mindrot.jbcrypt.BCrypt;

public class User {

    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private boolean isActive;
    private Date lastLoginAt;
    private Date createdAt;
    private Date updatedAt;

    // Constructor
    public User(int id, String username, String email,
                String passwordHash) {

        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;

        this.isActive = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Database Connection
    private static Connection getConnection()
            throws SQLException {

        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String database = System.getenv("DB_NAME");
        String dbUsername = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (host == null || port == null || database == null
                || dbUsername == null || password == null) {

            throw new SQLException(
                    "Database environment variables are missing."
            );
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL Connector/J is not installed.", e
            );
        }
// WWW.FACEBOOK.COM/
        String url = "jdbc:mysql://" + host + ":" + port
                + "/" + database
                + "?sslMode=REQUIRED&serverTimezone=UTC";

        return DriverManager.getConnection(
                url, dbUsername, password
        );
    }

    // Register User
    public static User register(String username,
                                String email,
                                String password)
            throws SQLException {

        if (username == null || username.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {

            throw new IllegalArgumentException(
                    "All required fields must be filled."
            );
        }

        // Encrypt Password
        String hashedPassword = BCrypt.hashpw(
                password,
                BCrypt.gensalt(12)
        );

        String sql = "INSERT INTO users "
                + "(username, email, password_hash) "
                + "VALUES (?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashedPassword);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                try (ResultSet keys = ps.getGeneratedKeys()) {

                    if (keys.next()) {

                        return new User(
                                keys.getInt(1),
                                username,
                                email,
                                hashedPassword
                        );
                    }
                }
            }
        }

        return null;
    }

    // Login
    public static User login(String email, String password)
            throws SQLException {

        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    User user = mapUser(rs);

                    if (!user.isActive()) {
                        return null;
                    }

                    if (user.checkPassword(password)) {

                        user.recordLogin();

                        return user;
                    }
                }
            }
        }

        return null;
    }

    // Check Password
    public boolean checkPassword(String password) {

        if (password == null || passwordHash == null) {
            return false;
        }

        return BCrypt.checkpw(password, passwordHash);
    }

    // Update Profile
    public void updateProfile() throws SQLException {

        String sql = "UPDATE users SET username = ?, "
                + "email = ?, display_name = ?, "
                + "avatar_url = ?, bio = ?, "
                + "updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, displayName);
            ps.setString(4, avatarUrl);
            ps.setString(5, bio);
            ps.setInt(6, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                this.updatedAt = new Date();
            }
        }
    }

    // Deactivate Account
    public void deactivate() throws SQLException {

        String sql = "UPDATE users SET is_active = FALSE, "
                + "updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                this.isActive = false;
                this.updatedAt = new Date();
            }
        }
    }

    // Record Login
    public void recordLogin() throws SQLException {

        String sql = "UPDATE users SET "
                + "last_login_at = CURRENT_TIMESTAMP, "
                + "updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                this.lastLoginAt = new Date();
                this.updatedAt = new Date();
            }
        }
    }

    // Get User By ID
    public static User getUserById(int userId)
            throws SQLException {

        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }

        return null;
    }

    // Map Database Result To User Object
    private static User mapUser(ResultSet rs) 
            throws SQLException {

        User user = new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password_hash")
        );

        user.displayName = rs.getString("display_name");
        user.avatarUrl = rs.getString("avatar_url");
        user.bio = rs.getString("bio");
        user.isActive = rs.getBoolean("is_active");

        Timestamp lastLogin = rs.getTimestamp("last_login_at");

        if (lastLogin != null) {
            user.lastLoginAt = new Date(lastLogin.getTime());
        }

        Timestamp created = rs.getTimestamp("created_at");

        if (created != null) {
            user.createdAt = new Date(created.getTime());
        }

        Timestamp updated = rs.getTimestamp("updated_at");

        if (updated != null) {
            user.updatedAt = new Date(updated.getTime());
        }

        return user;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isActive() {
        return isActive;
    }

    public Date getLastLoginAt() {
        return lastLoginAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    // ------------------------------------------------------------
    // Debug / Test
    // ------------------------------------------------------------
    public static void main(String[] args) {

        String email = "mohamed@gmail.com";
        String password = "12345678";

        try {

            // Step 1: show which database we are connected to
            // and every user stored in it
            try (Connection con = getConnection();
                 Statement st = con.createStatement();
                 ResultSet r = st.executeQuery(
                         "SELECT DATABASE() AS db, id, email, is_active "
                         + "FROM users")) {

                System.out.println("=== USERS IN DATABASE ===");

                boolean any = false;

                while (r.next()) {
                    any = true;
                    System.out.println(r.getString("db")
                            + " | id=" + r.getInt("id")
                            + " | [" + r.getString("email") + "]"
                            + " | active=" + r.getBoolean("is_active"));
                }

                if (!any) {
                    System.out.println("(table is empty)");
                }
            }

            // Step 2: check whether our test user exists
            boolean exists;

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT 1 FROM users WHERE email = ?")) {

                ps.setString(1, email);

                try (ResultSet rs = ps.executeQuery()) {
                    exists = rs.next();
                }
            }

            // Step 3: register it if missing
            if (!exists) {

                System.out.println("\nUser not found -> registering...");

                User u = register("mohamed", email, password);

                System.out.println(u != null
                        ? "Registered, id=" + u.getId()
                        : "Register FAILED!");
            } else {
                System.out.println("\nUser already exists.");
            }

            // Step 4: test the real login() method
            System.out.println("\n=== LOGIN TEST ===");

            User logged = login(email, password);

            if (logged != null) {
                System.out.println("LOGIN WORKS! Welcome "
                        + logged.getUsername()
                        + " (id=" + logged.getId() + ")");
            } else {
                System.out.println("LOGIN FAILED! "
                        + "(wrong password, inactive account, "
                        + "or invalid hash)");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}