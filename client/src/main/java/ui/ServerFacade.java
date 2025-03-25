package ui;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ServerFacade {
    private static final String BASE_URL = "http://localhost:8080";
    private final Gson gson = new Gson();
    //pregame
    public AuthData register(String username, String password, String email) throws ClientException {
        String path = "/user";
        RegisterRequest request = new RegisterRequest(username, password, email);
        String jsonInput = gson.toJson(request);
        String responseBody = sendRequest("POST", path, jsonInput, null); // No auth token for register
        AuthResponse response = gson.fromJson(responseBody, AuthResponse.class);
        return new AuthData(response.authToken, response.username);
    }

    public AuthData login(String username, String password) throws ClientException {
        String path = "/session";
        LoginRequest request = new LoginRequest(username, password);
        String jsonInput = gson.toJson(request);
        String responseBody = sendRequest("POST", path, jsonInput, null); // No auth token for login
        AuthResponse response = gson.fromJson(responseBody, AuthResponse.class);
        return new AuthData(response.authToken, response.username);
    }
    //pastgame
    public void logout(String authToken) throws ClientException {
        String path = "/session";
        sendRequest("DELETE", path, null, authToken);
    }

    public int createGame(String authToken, String gameName) throws ClientException {
        String path = "/game";
        CreateGameRequest request = new CreateGameRequest(gameName);
        String jsonInput = gson.toJson(request);
        String responseBody = sendRequest("POST", path, jsonInput, authToken);
        CreateGameResult result = gson.fromJson(responseBody, CreateGameResult.class);
        return result.gameID;
    }
    public List<GameData> listGames(String authToken) throws ClientException {
        String path = "/game";
        String responseBody = sendRequest("GET", path, null, authToken);
        GameListResult result = gson.fromJson(responseBody, GameListResult.class);
        return result.games;
    }
    public GameData joinGame(String authToken, String playerColor, int gameID) throws ClientException {
        String path = "/game";
        JoinGameRequest request = new JoinGameRequest(playerColor, gameID);
        String jsonInput = gson.toJson(request);
        sendRequest("PUT", path, jsonInput, authToken);
        return getGame(authToken, gameID);
    }
    public GameData getGame(String authToken, int gameID) throws ClientException {
        List<GameData> games = listGames(authToken);
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new ClientException("Game not found");
    }

    public String sendRequest(String method, String path, String jsonInput, String authToken) throws ClientException {
        try {
            URL url = new URL(BASE_URL + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            if (authToken != null) {
                conn.setRequestProperty("Authorization", authToken);
            }
            if (jsonInput != null) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonInput.getBytes());
                    os.flush();
                }
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                return readStream(conn.getInputStream());
            } else {
                String errorBody = readStream(conn.getErrorStream());
                ErrorResponse error = gson.fromJson(errorBody, ErrorResponse.class);
                throw new ClientException(error.message);
            }
        } catch (Exception e) {
            throw new ClientException("Error communicating with server: " + e.getMessage());
        }
    }
    private String readStream(java.io.InputStream is) throws java.io.IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }


    record RegisterRequest(String username, String password, String email) {}
    record LoginRequest(String username, String password) {}
    record AuthResponse(String username, String authToken) {}
    record ErrorResponse(String message) {}
    record CreateGameRequest(String gameName) {}
    record CreateGameResult(int gameID) {}
    record GameListResult(List<GameData> games) {}
    record JoinGameRequest(String playerColor, int gameID) {}
}
