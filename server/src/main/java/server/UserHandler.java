package server;

import org.mindrot.jbcrypt.BCrypt;
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
        response.type("application/json");
        String path = request.pathInfo();
        String method = request.requestMethod();

        try {
            if (path.equals("/user") && method.equals("POST")) {
                return registerUser(request, response);
            }
            if (path.equals("/session") && method.equals("POST")) {
                return loginUser(request, response);
            }
            if (path.equals("/session") && method.equals("DELETE")) {
                return logoutUser(request, response);
            }
            // Wrong path or method
            response.status(404);
            ErrorMessage error = new ErrorMessage("Error: endpoint not found");
            return gson.toJson(error);

        } catch (DataAccessException e) {
            String msg = e.getMessage();
            if (msg.equals("bad request")) {
                response.status(400);
                ErrorMessage error = new ErrorMessage("Error: missing or invalid request data");
                return gson.toJson(error);
            }
            if (msg.equals("unauthorized")) {
                response.status(401);
                ErrorMessage error = new ErrorMessage("Error: invalid or missing authentication token");
                return gson.toJson(error);
            }
            if (msg.equals("already taken")) {
                response.status(403);
                ErrorMessage error = new ErrorMessage("Error: username already taken");
                return gson.toJson(error);
            }
            if (msg.equals("user not exist")) {
                response.status(401);
                ErrorMessage error = new ErrorMessage("Error: user does not exist");
                return gson.toJson(error);
            }
            if (msg.equals("wrong password")) {
                response.status(401);
                ErrorMessage error = new ErrorMessage("Error: incorrect password");
                return gson.toJson(error);
            }
            response.status(500);
            ErrorMessage error = new ErrorMessage("Error: database operation failed - " + msg);
            return gson.toJson(error);
        } catch (Exception e) {
            response.status(500);
            ErrorMessage error = new ErrorMessage("Error: unexpected server error - " + e.getMessage());
            return gson.toJson(error);
        }
    }

    private Object registerUser(Request request, Response response) throws DataAccessException {
        RegisterBody body = gson.fromJson(request.body(), RegisterBody.class);
        if (body == null || body.username == null || body.password == null) {
            throw new DataAccessException("bad request");
        }

        String username = body.username;
        String password = body.password;
        String email = body.email;
        UserData user = new UserData(username, password, email);
        userDAO.createUser(user);

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        authDAO.createAuth(auth);

        response.status(200);
        RegisterResult result = new RegisterResult(username, token);
        return gson.toJson(result);
    }

    private Object loginUser(Request request, Response response) throws DataAccessException {
        LoginBody body = gson.fromJson(request.body(), LoginBody.class);
        if (body == null || body.username == null || body.password == null) {
            throw new DataAccessException("bad request");
        }

        String username = body.username;
        String password = body.password;
        UserData user = userDAO.getUser(username);

        if (user == null) {
            throw new DataAccessException("user not exist");
        }

        String storedPassword = user.password();
        if (!BCrypt.checkpw(password, storedPassword)) {
            throw new DataAccessException("wrong password");
        }

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        authDAO.createAuth(auth);

        response.status(200);
        LoginResult result = new LoginResult(username, token);
        return gson.toJson(result);
    }

    private Object logoutUser(Request request, Response response) throws DataAccessException {
        String token = request.headers("authorization");
        if (token == null || token.isBlank()) {
            throw new DataAccessException("unauthorized");
        }

        AuthData auth = authDAO.getAuth(token);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        authDAO.deleteAuth(token);
        response.status(200);
        return "{}";
    }

    record ErrorMessage(String message) {}
    record RegisterBody(String username, String password, String email) {}
    record LoginBody(String username, String password) {}
    record RegisterResult(String username, String authToken) {}
    record LoginResult(String username, String authToken) {}
}