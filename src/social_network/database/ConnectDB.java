package social_network.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public final class ConnectDB {

    private ConnectDB() { }


    public static final Connection getConnection() throws SQLException {
        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String database = System.getenv("DB_NAME");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (host == null || port == null || database == null
                || user == null || password == null) {
            throw new SQLException("Database environment variables are missing.");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Connector/J is not installed.", e);
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?sslMode=REQUIRED&serverTimezone=UTC";
        return DriverManager.getConnection(url, user, password);
    }
}