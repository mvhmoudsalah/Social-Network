package social_network.model.enums;

/** Matches the MySQL ENUM used by post_media.media_type. */
public enum MediaType {
    IMAGE,
    VIDEO;

    /** Value stored in the database (lowercase). */
    public String toDb() {
        return name().toLowerCase();
    }

    /** Reads the database value (case-insensitive). */
    public static MediaType fromDb(String value) {
        return valueOf(value.trim().toUpperCase());
    }
}