package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;

// service for register, login, and  logout

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(String username, String password, String email)
            throws DataAccessException {
        // check if the password and username is null
        if (username == null || password == null) {
            throw new DataAccessException("bad request: username or password is blank");
        }
        // Create the user
        var newUser = new UserData(username, password, email);
        userDAO.createUser(newUser);

        // Create auth token
        String token = UUID.randomUUID().toString();
        var auth = new AuthData(token, username);
        authDAO.createAuth(auth);

        return auth;
    }
    public AuthData login(String username, String password) throws DataAccessException {
        // check if the password and username is null
        if (username == null || password == null) {
            throw new DataAccessException("bad request: username or password is blank");
        }
        // check if authorized
        var existingUser = userDAO.getUser(username);
        if (existingUser == null || existingUser.password() != password) {
            throw new DataAccessException("unauthorized: invalid username/password");
        }
        String token = UUID.randomUUID().toString();
        var auth = new AuthData(token, username);
        authDAO.createAuth(auth);

        return auth;
    }

    public void logout(String token) throws DataAccessException {
        // check if the password and username is null
        if (token == null) {
            throw new DataAccessException("unauthorized: missing token");
        }
        var existingAuth = authDAO.getAuth(token);
        if (existingAuth == null) {
            throw new DataAccessException("unauthorized: token not found");
        }
        authDAO.deleteAuth(token);
    }
}