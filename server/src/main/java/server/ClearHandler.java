package server;

import spark.Request;
import spark.Response;
import spark.Route;
import service.ClearService;
import dataaccess.DataAccessException;
import com.google.gson.Gson;

public class ClearHandler implements Route {
    private final Gson gson = new Gson();
    private final ClearService clearService;  // Note: renamed to follow Java naming conventions

    // Constructor to initialize clearService
    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;  // Assign the parameter to the field
    }

    @Override
    public Object handle(Request request, Response response) {
        try {
            clearService.clear();  // Now safe to use clearService
            response.status(200);
            return "{}";
        } catch (DataAccessException e) {
            response.status(500);
            var error = new ErrorMessage("Error: " + e.getMessage());
            return gson.toJson(error);
        }
    }

    record ErrorMessage(String message) {}
}