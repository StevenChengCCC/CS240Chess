package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import spark.Request;
import spark.Response;
import spark.Route;

public abstract class BaseHandler implements Route {
    protected final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        try {
            return handleRequest(request, response);
        } catch (DataAccessException e) {
            return handleDataAccessException(e, response);
        } catch (Exception e) {
            response.status(500);
            ErrorMessage error = new ErrorMessage("Error: unexpected server error - " + e.getMessage());
            return gson.toJson(error);
        }
    }

    // Subclasses implement this to define specific request-handling logic
    protected abstract Object handleRequest(Request request, Response response) throws DataAccessException;

    // Common error handling for DataAccessException
    protected String handleDataAccessException(DataAccessException e, Response response) {
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
            ErrorMessage error = new ErrorMessage(getAlreadyTakenMessage());
            return gson.toJson(error);
        }
        response.status(500);
        ErrorMessage error = new ErrorMessage("Error: database operation failed - " + msg);
        return gson.toJson(error);
    }

    // Subclasses provide their own "already taken" message
    protected abstract String getAlreadyTakenMessage();

    // Error message record used across handlers
    record ErrorMessage(String message) {}
}