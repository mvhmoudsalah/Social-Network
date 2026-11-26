
package social_network.model.enums;


public enum PostVisibility {
        PUBLIC, FRIENDS, PRIVATE;

        public String toDb() {
            return name().toLowerCase();
        }

        public static PostVisibility fromDb(String value) {
            return valueOf(value.trim().toUpperCase());
        }
    }
