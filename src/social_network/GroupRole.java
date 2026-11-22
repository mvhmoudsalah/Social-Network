package social_network;

/** Matches the MySQL ENUM used by group_members.role. */
public enum GroupRole {
    OWNER,
    ADMIN,
    MEMBER;

    /** Value stored in the database (lowercase). */
    public String toDb() {
        return name().toLowerCase();
    }

    /** Reads the database value (case-insensitive). */
    public static GroupRole fromDb(String value) {
        return valueOf(value.trim().toUpperCase());
    }
}