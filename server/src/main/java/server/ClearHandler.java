package server;

import spark.Request;
import spark.Response;
import spark.Route;

import dataaccess.MemoryDatabase;
import com.google.gson.Gson;

public class ClearHandler implements Route {
    private final Gson gson = new Gson();

    @Override
    public Object handle(Request request, Response response) {
        try {
            MemoryDatabase.clearAll();
            response.status(200);
            return "{}"; // empty JSON object
        } catch (Exception e) {
            response.status(500);
            var error = new ErrorMessage("Error: " + e.getMessage());
            return gson.toJson(error);
        }
    }

    record ErrorMessage(String message) {}
}
