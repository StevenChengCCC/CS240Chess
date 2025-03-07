package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    // Our data access objects
    private UserDAO myUserDAO;
    private AuthDAO myAuthDAO;

    // Constructor to set up the DAOs
    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.myUserDAO = userDAO;
        this.myAuthDAO = authDAO;
    }

    // Register a new user
    public AuthData register(String username, String password, String email) throws DataAccessException {
        // Check if username or password is missing
        if (username == null) {
            throw new DataAccessException("bad request: username is blank");
        }
        if (password == null) {
            throw new DataAccessException("bad request: password is blank");
        }

        // Make a new user object
        UserData newUser = new UserData(username, password, email);
        // Save the user in the database
        myUserDAO.createUser(newUser);

        // Make a new random token
        String newToken = UUID.randomUUID().toString();
        // Make an auth object with the token and username
        AuthData authStuff = new AuthData(newToken, username);
        // Save the auth in the database
        myAuthDAO.createAuth(authStuff);

        // Return the auth object
        return authStuff;
    }

    // Log in a user
    public AuthData login(String username, String password) throws DataAccessException {
        // Check if username or password is missing
        if (username == null) {
            throw new DataAccessException("bad request: username is blank");
        }
        if (password == null) {
            throw new DataAccessException("bad request: password is blank");
        }

        // Try to find the user in the database
        UserData foundUser = myUserDAO.getUser(username);
        // Check if user exists and password matches
        if (foundUser == null) {
            throw new DataAccessException("unauthorized: invalid username/password");
        }
        String storedPassword = foundUser.password();
        if (!storedPassword.equals(password)) {
            throw new DataAccessException("unauthorized: invalid username/password");
        }

        // Make a new random token
        String newToken = UUID.randomUUID().toString();
        // Make an auth object
        AuthData authStuff = new AuthData(newToken, username);
        // Save the auth in the database
        myAuthDAO.createAuth(authStuff);

        // Return the auth object
        return authStuff;
    }

    // Log out a user
    public void logout(String token) throws DataAccessException {
        // Check if token is missing
        if (token == null) {
            throw new DataAccessException("unauthorized: missing token");
        }

        // Look for the token in the database
        AuthData foundAuth = myAuthDAO.getAuth(token);
        // Check if token exists
        if (foundAuth == null) {
            throw new DataAccessException("unauthorized: token not found");
        }

        // Remove the token from the database
        myAuthDAO.deleteAuth(token);
    }
}