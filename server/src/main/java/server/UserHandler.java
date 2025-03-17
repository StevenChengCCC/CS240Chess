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
            response.status(200);
            String errorMsg = "Error: not found";
            ErrorMessage error = new ErrorMessage(errorMsg);
            String jsonError = gson.toJson(error);
            return jsonError;

        } catch (DataAccessException e) {
            String msg = e.getMessage();
            if (msg.equals("bad request")) {
                response.status(400);
                String errorMsg = "Error: bad request";
                ErrorMessage error = new ErrorMessage(errorMsg);
                String jsonError = gson.toJson(error);
                return jsonError;
            }
            if (msg.equals("unauthorized")) {
                response.status(401);
                String errorMsg = "Error: unauthorized";
                ErrorMessage error = new ErrorMessage(errorMsg);
                String jsonError = gson.toJson(error);
                return jsonError;
            }
            response.status(403);
            String errorMsg = "Error: " + msg;
            ErrorMessage error = new ErrorMessage(errorMsg);
            String jsonError = gson.toJson(error);
            return jsonError;
        } catch (Exception e) {
            response.status(401);
            String errorMsg = "Error: " + e.getMessage();
            ErrorMessage error = new ErrorMessage(errorMsg);
            String jsonError = gson.toJson(error);
            return jsonError;
        }
    }

    private Object registerUser(Request request, Response response) throws DataAccessException {
        RegisterBody body = gson.fromJson(request.body(), RegisterBody.class);
        if (body == null || body.username == null || body.password == null) {
            response.status(400);
            String errorMsg = "Error: bad request";
            ErrorMessage error = new ErrorMessage(errorMsg);
            String jsonError = gson.toJson(error);
            return jsonError;
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
        String jsonResult = gson.toJson(result);
        return jsonResult;
    }

    private Object loginUser(Request request, Response response) throws DataAccessException {
        LoginBody body = gson.fromJson(request.body(), LoginBody.class);
        if (body == null || body.username == null || body.password == null) {
            response.status(400);
            String errorMsg = "Error: bad request";
            ErrorMessage error = new ErrorMessage(errorMsg);
            String jsonError = gson.toJson(error);
            return jsonError;
        }

        String username = body.username;
        String password = body.password;
        UserData user = userDAO.getUser(username);
        String storedPassword = user.password();
        if (user == null || !BCrypt.checkpw(password, storedPassword)){
            response.status(401);
            String errorMsg = "Error: unauthorized";
            ErrorMessage error = new ErrorMessage(errorMsg);
            String jsonError = gson.toJson(error);
            return jsonError;
        }

        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        authDAO.createAuth(auth);

        response.status(200);
        LoginResult result = new LoginResult(username, token);
        String jsonResult = gson.toJson(result);
        return jsonResult;
    }

    private Object logoutUser(Request request, Response response) throws DataAccessException {
        String token = request.headers("authorization");
        if (token == null || token.isBlank()) {
            response.status(401);
            String errorMsg = "Error: unauthorized";
            ErrorMessage error = new ErrorMessage(errorMsg);
            String jsonError = gson.toJson(error);
            return jsonError;
        }

        AuthData auth = authDAO.getAuth(token);
        if (auth == null) {
            response.status(401);
            String errorMsg = "Error: unauthorized";
            ErrorMessage error = new ErrorMessage(errorMsg);
            String jsonError = gson.toJson(error);
            return jsonError;
        }

        authDAO.deleteAuth(token);
        response.status(200);
        String emptyJson = "{}";
        return emptyJson;
    }

    record ErrorMessage(String message) {}
    record RegisterBody(String username, String password, String email) {}
    record LoginBody(String username, String password) {}
    record RegisterResult(String username, String authToken) {}
    record LoginResult(String username, String authToken) {}
}