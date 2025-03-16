package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MySQLAuthDAO implements AuthDAO{

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO auths (auth_token, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());
            statement.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Error creating auth: " + e.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        String sql = "SELECT auth_token, username FROM auths WHERE auth_token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, token);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String dbToken = rs.getString("auth_token");
                String username = rs.getString("username");
                return new AuthData(dbToken, username);
            }
            return null;
        } catch (Exception e) {
            throw new DataAccessException("Error getting auth: " + e.getMessage());
        }
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        String sql = "DELETE FROM auths WHERE auth_token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, token);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Auth token not found");
            }
        } catch (Exception e) {
            throw new DataAccessException("Error deleting auth: " + e.getMessage());
        }
    }
}