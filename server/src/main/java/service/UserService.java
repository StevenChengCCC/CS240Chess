package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;

// service for register login logout

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
}