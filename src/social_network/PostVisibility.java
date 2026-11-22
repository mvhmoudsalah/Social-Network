
package social_network;


public enum PostVisibility {
        PUBLIC, FRIENDS, PRIVATE;

        public String toDb() {
            return name().toLowerCase();
        }

        public static PostVisibility fromDb(String value) {
            return valueOf(value.trim().toUpperCase());
        }
    }
