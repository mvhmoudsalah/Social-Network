
package social_network;

public class TestDatabase {

    public static void main(String[] args) {

        System.out.println("========== DATABASE ENVIRONMENT TEST ==========");

        System.out.println("HOST: " + System.getenv("DB_HOST"));

        System.out.println("PORT: " + System.getenv("DB_PORT"));

        System.out.println("DATABASE: " + System.getenv("DB_NAME"));

        System.out.println("USERNAME EXISTS: "
                + (System.getenv("DB_USER") != null));

        System.out.println("PASSWORD EXISTS: "
                + (System.getenv("DB_PASSWORD") != null));

        System.out.println("================================================");
    }
}
