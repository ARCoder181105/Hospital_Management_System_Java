// dal/DatabaseConnection.java (Updated and Corrected)
package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // --- IMPORTANT: UPDATE THESE VALUES FOR YOUR DATABASE ---
    private static final String URL = "jdbc:postgresql://localhost:5432/hospital_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123456789";

    /**
     * This method provides a new, active connection to the database.
     * It is the responsibility of the caller to close this connection when done.
     * (This is handled automatically by the try-with-resources blocks in DataAccess).
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            // This is a critical error, so we wrap it in a SQLException
            throw new SQLException("PostgreSQL JDBC Driver not found.", e);
        }
        // Return a new connection every time the method is called
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}