package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MySQLUserDAO implements UserDAO {
    @Override
    public void createUser(UserData user) throws DataAccessException {
        // Check if the username already exists
        if (getUser(user.username()) != null) {
            throw new DataAccessException("already taken");
        }

        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            statement.setString(1, user.username());
            statement.setString(2, hashedPassword);
            statement.setString(3, user.email());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Error creating user: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String dbUsername = rs.getString("username");
                String hashedPassword = rs.getString("password");
                String email = rs.getString("email");
                return new UserData(dbUsername, hashedPassword, email);
            }
            return null;
        } catch (Exception e) {
            throw new DataAccessException("Error getting user: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM users";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Error clearing users: " + e.getMessage());
        }
    }
}