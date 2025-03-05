package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import dataaccess.MemoryDatabase;

// service for clearing all data in the server database.

public class AuthService {
    private final AuthDAO authDAO;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;


    public AuthService(AuthDAO authDAO, UserDAO userDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
    }

    //Clears all data: users, auth tokens, games.
    public void clear() throws DataAccessException {
        MemoryDatabase.clearAll();
    }
}
