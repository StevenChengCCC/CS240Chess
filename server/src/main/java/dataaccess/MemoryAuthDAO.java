package dataaccess;

import model.AuthData;

public class MemoryAuthDAO implements AuthDAO {
    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        // Overwrite any existing token with new data
        MemoryDatabase.AUTHS.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        return MemoryDatabase.AUTHS.get(token);  // may be null
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        MemoryDatabase.AUTHS.remove(token);  // no exception if not found
    }
}
