package ui;

import com.google.gson.Gson;

public class ServerFacade {
    private static final String BASE_URL = "http://localhost:8080"; // Adjust if your server uses a different port
    private final Gson gson = new Gson();
}