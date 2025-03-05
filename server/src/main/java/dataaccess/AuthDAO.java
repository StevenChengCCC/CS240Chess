package dataaccess;

import model.AuthData;

public interface AuthDAO {
    // Create and store a new AuthData
    void createAuth(AuthData auth) throws DataAccessException;

    // Retrieve an AuthData given the token
    AuthData getAuth(String token) throws DataAccessException;

    // Delete an AuthData (logout)
    void deleteAuth(String token) throws DataAccessException;
}
