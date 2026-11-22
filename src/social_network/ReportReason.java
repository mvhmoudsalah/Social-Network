package social_network;

/** Matches the MySQL ENUM used by reports.reason. */
public enum ReportReason {
    SPAM,
    HARASSMENT,
    HATE_SPEECH,
    VIOLENCE,
    NUDITY,
    OTHER;

    /** Value stored in the database (lowercase). */
    public String toDb() {
        return name().toLowerCase();
    }

    /** Reads the database value (case-insensitive). */
    public static ReportReason fromDb(String value) {
        return valueOf(value.trim().toUpperCase());
    }
}