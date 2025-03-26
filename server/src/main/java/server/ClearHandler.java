package server;

import spark.Request;
import spark.Response;
import spark.Route;
import service.ClearService;
import dataaccess.DataAccessException;
import com.google.gson.Gson;

public class ClearHandler implements Route {
    private final Gson gson = new Gson();
    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            clearService.clear();
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            response.status(500);
            ErrorMessage error = new ErrorMessage("Error: database operation failed - " + e.getMessage());
            return gson.toJson(error);
        } catch (Exception e) {
            response.status(500);
            ErrorMessage error = new ErrorMessage("Error: unexpected server error - " + e.getMessage());
            return gson.toJson(error);
        }
    }

    record ErrorMessage(String message) {}
}