package server;

import spark.Request;
import spark.Response;
import spark.Route;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;

import model.UserData;
import model.AuthData;

import com.google.gson.Gson;

import java.util.UUID;

public class UserHandler implements Route {
    private final Gson gson = new Gson();
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserHandler(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }
    @Override
    public Object handle(Request request, Response response) {
        // For example, path might be /user or /session
        String path = request.pathInfo();
        String method = request.requestMethod(); // GET, POST, DELETE
        response.type("application/json");

        try {
            if ("/user".equals(path) && "POST".equals(method)) {
                return handleRegister(request, response);
            } else if ("/session".equals(path) && "POST".equals(method)) {
                return handleLogin(request, response);
            } else if ("/session".equals(path) && "DELETE".equals(method)) {
                return handleLogout(request, response);
            } else {
                response.status(200);
                return gson.toJson(new ErrorMessage("Error: not found"));
            }
        } catch (Exception e) {
            response.status(403);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private Object handleRegister(Request request, Response response) throws DataAccessException {
        // parse JSON
        RegisterBody body = gson.fromJson(request.body(), RegisterBody.class);
        if (body == null || body.username == null || body.password == null) {
            response.status(400);
            return gson.toJson(new ErrorMessage("Error: bad request"));
        }
        // create the user
        UserData newUser = new UserData(body.username, body.password, body.email);
        //already exists
        userDAO.createUser(newUser);

        // create token
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, body.username);
        authDAO.createAuth(auth);

        // success
        response.status(200);
        var result = new RegisterResult(body.username, token);
        return gson.toJson(result);
    }

    private Object handleLogin(Request request, Response response) throws DataAccessException {
        LoginBody body = gson.fromJson(request.body(), LoginBody.class);
        if (body == null || body.username == null || body.password == null) {
            response.status(400);
            return gson.toJson(new ErrorMessage("Error: bad request"));
        }

        // get user
        UserData user = userDAO.getUser(body.username);
        if (user == null || !user.password().equals(body.password)) {
            // unauthorized
            response.status(401);
            return gson.toJson(new ErrorMessage("Error: unauthorized"));
        }
        // generate token
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, user.username());
        authDAO.createAuth(auth);

        response.status(200);
        var result = new LoginResult(user.username(), token);
        return gson.toJson(result);
    }

    private Object handleLogout(Request request, Response response) throws DataAccessException {
        // get auth token from header
        String token = request.headers("authorization");
        if (token == null || token.isBlank()) {
            // unauthorized
            response.status(401);
            return gson.toJson(new ErrorMessage("Error: unauthorized"));
        }
        // see if token exists
        AuthData auth = authDAO.getAuth(token);
        if (auth == null) {
            // unauthorized
            response.status(401);
            return gson.toJson(new ErrorMessage("Error: unauthorized"));
        }
        // delete the token
        authDAO.deleteAuth(token);
        // success
        response.status(200);
        return "{}";
    }
    record ErrorMessage(String message) {}

    // Request bodies
    record RegisterBody(String username, String password, String email) {}
    record LoginBody(String username, String password) {}
    // Response bodies
    record RegisterResult(String username, String authToken) {}
    record LoginResult(String username, String authToken) {}
}
