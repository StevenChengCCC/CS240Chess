package dataaccess;

import model.UserData;

public interface UserDAO {
    // Create a new user in the data store
    void createUser(UserData user) throws DataAccessException;
    // Retrieve an existing user by username
    UserData getUser(String username) throws DataAccessException;
}
