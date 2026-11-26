package social_network.model.enums;

/** Matches the MySQL ENUM used by friend_requests.status. */
public enum FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    /** Value stored in the database (lowercase). */
    public String toDb() {
        return name().toLowerCase();
    }

    /** Reads the database value (case-insensitive). */
    public static FriendRequestStatus fromDb(String value) {
        return valueOf(value.trim().toUpperCase());
    }
}