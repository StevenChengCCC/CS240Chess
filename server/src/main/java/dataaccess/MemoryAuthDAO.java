package dataaccess;

import model.AuthData;

public class MemoryAuthDAO implements AuthDAO {
    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        MemoryDatabase.AUTHS.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        return MemoryDatabase.AUTHS.get(token);
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        MemoryDatabase.AUTHS.remove(token);
    }
}
