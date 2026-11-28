package social_network.model.system;


import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import social_network.database.ConnectDB;
import social_network.model.enums.ReportReason;
import social_network.model.enums.ReportStatus;

/**
 * OOP + DB in one class. Table: REPORTS
 * A report targets exactly ONE thing: a post OR a user (CHECK constraint in the DB).
 */
public class Report {

    // ---------- fields ----------
    private int id;                       // 0 = not saved yet
    private int reporterId;
    private Integer postId;               // nullable
    private Integer reportedUserId;       // nullable
    private ReportReason reason;
    private String details;               // nullable
    private ReportStatus status = ReportStatus.OPEN;
    private Date createdAt;
    private Date resolvedAt;              // nullable

    // ---------- constructors (use the factories below) ----------
    private Report(int reporterId, Integer postId, Integer reportedUserId, ReportReason reason, String details) {
        if ((postId == null) == (reportedUserId == null))
            throw new IllegalArgumentException("A report must target exactly one post or one user");
        if (reason == null) throw new IllegalArgumentException("Reason is required");
        this.reporterId = reporterId;
        this.postId = postId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.details = details;
    }

    private Report(ResultSet rs) throws SQLException {
        this.id = rs.getInt("id");
        this.reporterId = rs.getInt("reporter_id");
        this.postId = rs.getObject("post_id", Integer.class);
        this.reportedUserId = rs.getObject("reported_user_id", Integer.class);
        this.reason = ReportReason.fromDb(rs.getString("reason"));
        this.details = rs.getString("details");
        this.status = ReportStatus.fromDb(rs.getString("status"));
        this.createdAt = rs.getTimestamp("created_at");
        this.resolvedAt = rs.getTimestamp("resolved_at");
    }

    // ---------- factories ----------
    public static Report forPost(int reporterId, int postId, ReportReason reason, String details) {
        return new Report(reporterId, postId, null, reason, details);
    }

    public static Report forUser(int reporterId, int reportedUserId, ReportReason reason, String details) {
        if (reporterId == reportedUserId)
            throw new IllegalArgumentException("You cannot report yourself");
        return new Report(reporterId, null, reportedUserId, reason, details);
    }

    // ---------- OOP methods ----------
    /** Sets the new status. RESOLVED and DISMISSED also stamp resolvedAt. Call save() afterwards. */
    public void resolve(ReportStatus newStatus) {
        this.status = newStatus;
        this.resolvedAt = (newStatus == ReportStatus.RESOLVED || newStatus == ReportStatus.DISMISSED)
                ? new Date() : null;
    }

    public boolean isPostReport() { return postId != null; }

    // ---------- DB: instance methods ----------
    /**
     * INSERT if new, otherwise UPDATE status and resolved_at.
     * Throws SQLIntegrityConstraintViolationException if the same reporter already
     * reported the same post or user (UNIQUE constraints).
     */
    public void save() throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            if (id == 0) {
                String sql = "INSERT INTO reports (reporter_id, post_id, reported_user_id, reason, details) "
                           + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, reporterId);
                    if (postId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, postId);
                    if (reportedUserId == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, reportedUserId);
                    ps.setString(4, reason.toDb());
                    if (details == null) ps.setNull(5, Types.VARCHAR); else ps.setString(5, details);
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) id = k.getInt(1); }
                }
            } else {
                String sql = "UPDATE reports SET status = ?, resolved_at = ? WHERE id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, status.toDb());
                    if (resolvedAt == null) ps.setNull(2, Types.TIMESTAMP);
                    else ps.setTimestamp(2, new Timestamp(resolvedAt.getTime()));
                    ps.setInt(3, id);
                    ps.executeUpdate();
                }
            }
        }
    }

    public boolean delete() throws SQLException {
        if (id == 0) return false;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM reports WHERE id = ?")) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) id = 0;
            return ok;
        }
    }

    // ---------- DB: static finders ----------
    public static Report findById(int id) throws SQLException {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM reports WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? new Report(rs) : null; }
        }
    }

    /** The moderation queue, oldest first (uses idx_reports_status). */
    public static List<Report> findByStatus(ReportStatus status) throws SQLException {
        return query("SELECT * FROM reports WHERE status = ? ORDER BY created_at ASC", status.toDb());
    }

    /** All reports on a post (uses idx_reports_post). */
    public static List<Report> findByPost(int postId) throws SQLException {
        return query("SELECT * FROM reports WHERE post_id = ? ORDER BY created_at DESC", postId);
    }

    /** All reports on a user (uses idx_reports_user). */
    public static List<Report> findByReportedUser(int userId) throws SQLException {
        return query("SELECT * FROM reports WHERE reported_user_id = ? ORDER BY created_at DESC", userId);
    }

    public static int countByStatus(ReportStatus status) throws SQLException {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM reports WHERE status = ?")) {
            ps.setString(1, status.toDb());
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    private static List<Report> query(String sql, Object param) throws SQLException {
        List<Report> list = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(new Report(rs)); }
        }
        return list;
    }

    // ---------- getters ----------
    public int getId() { return id; }
    public int getReporterId() { return reporterId; }
    public Integer getPostId() { return postId; }
    public Integer getReportedUserId() { return reportedUserId; }
    public ReportReason getReason() { return reason; }
    public String getDetails() { return details; }
    public ReportStatus getStatus() { return status; }
    public Date getCreatedAt() { return createdAt; }
    public Date getResolvedAt() { return resolvedAt; }
}
