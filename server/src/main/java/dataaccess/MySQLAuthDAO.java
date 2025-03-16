package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
public class MySQLAuthDAO implements AuthDAO{

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        String sql = "INSERT INTO auths (auth_token, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement Statement = conn.prepareStatement(sql)) {
            Statement.setString(1, auth.authToken());
            Statement.setString(2, auth.username());
            Statement.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Error creating auth: " + e.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {

    }
}