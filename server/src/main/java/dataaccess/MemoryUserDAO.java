package dataaccess;

import model.UserData;

public class MemoryUserDAO implements UserDAO {
    @Override
    public void createUser(UserData user) throws DataAccessException {
        String username = user.username();
        if (username == null) {
            throw new DataAccessException("Username cannot be null or blank");
        }
        if (MemoryDatabase.USERS.containsKey(username)) {
            throw new DataAccessException("User already exists");
        }
        MemoryDatabase.USERS.put(username, user);
    }
    @Override
    public UserData getUser(String username) throws DataAccessException {
        return MemoryDatabase.USERS.get(username); // may be null
    }
}
