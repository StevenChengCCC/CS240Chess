package dataaccess;

import java.sql.*;

public class DatabaseInitializer {
    private static final String[] CREATE_TABLE_STATEMENTS = {
            "CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(255) PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255)" +
                    ")",
            "CREATE TABLE IF NOT EXISTS auth (" +
                    "auth_token VARCHAR(255) PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL, " +
                    "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE" +
                    ")",
            "CREATE TABLE IF NOT EXISTS games (" +
                    "game_id INT PRIMARY KEY, " +
                    "white_username VARCHAR(255), " +
                    "black_username VARCHAR(255), " +
                    "game_name VARCHAR(255) NOT NULL, " +
                    "game_data TEXT NOT NULL, " +
                    "FOREIGN KEY (white_username) REFERENCES users(username) ON DELETE SET NULL, " +
                    "FOREIGN KEY (black_username) REFERENCES users(username) ON DELETE SET NULL" +
                    ")"
    };

    public static void initialize() throws DataAccessException {
        // Create the database if it doesn't exist
        DatabaseManager.createDatabase();

        // Create tables if they don't exist
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String sql : CREATE_TABLE_STATEMENTS) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error initializing database: " + e.getMessage());
        }
    }
}