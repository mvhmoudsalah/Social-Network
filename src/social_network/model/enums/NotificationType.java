package social_network.model.enums;

/** Matches the MySQL ENUM used by notifications.type. */
public enum NotificationType {
    LIKE,
    COMMENT,
    FRIEND_REQUEST,
    FRIEND_ACCEPT,
    FOLLOW,
    SHARE,
    MESSAGE;

    /** Value stored in the database (lowercase). */
    public String toDb() {
        return name().toLowerCase();
    }

    /** Reads the database value (case-insensitive). */
    public static NotificationType fromDb(String value) {
        return valueOf(value.trim().toUpperCase());
    }
}