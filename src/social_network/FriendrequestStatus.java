package social_network;

/** Matches the MySQL ENUM used by friend_requests.status. */
public enum FriendrequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    /** Value stored in the database (lowercase). */
    public String toDb() {
        return name().toLowerCase();
    }

    /** Reads the database value (case-insensitive). */
    public static FriendrequestStatus fromDb(String value) {
        return valueOf(value.trim().toUpperCase());
    }
}