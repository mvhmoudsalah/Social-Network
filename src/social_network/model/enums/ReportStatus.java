package social_network.model.enums;

/** Matches the MySQL ENUM used by reports.status. */
public enum ReportStatus {
    OPEN,
    REVIEWING,
    RESOLVED,
    DISMISSED;

    /** Value stored in the database (lowercase). */
    public String toDb() {
        return name().toLowerCase();
    }

    /** Reads the database value (case-insensitive). */
    public static ReportStatus fromDb(String value) {
        return valueOf(value.trim().toUpperCase());
    }
}