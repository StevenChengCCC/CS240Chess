package server;

import spark.Request;
import spark.Response;
import spark.Route;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import com.google.gson.Gson;
import dataaccess.GameDAO;
import service.GameService;
import model.GameData;
import java.util.List;


public class GameHandler implements Route {
    private final Gson gson = new Gson();
    private final GameService gameService;

    public GameHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.gameService = new GameService(authDAO, gameDAO);
    }

    @Override
    public Object handle(Request request, Response response) {
        response.type("application/json");
        String path = request.pathInfo();
        String method = request.requestMethod();

        try {
            if (path.equals("/game") && method.equals("GET")) {
                // List games! GET /game
                String token = request.headers("authorization"); // Spec says header "authorization"
                List<GameData> games = gameService.listGames(token);
                response.status(200); // Spec says 200 for success
                GameListResult result = new GameListResult(games);
                String jsonResult = gson.toJson(result);
                return jsonResult;
            }

            if (path.equals("/game") && method.equals("POST")) {
                // Create a game! POST /game
                String token = request.headers("authorization"); // Spec says header "authorization"
                CreateGameBody body = gson.fromJson(request.body(), CreateGameBody.class);
                String gameName = body.gameName;
                int gameID = gameService.createGame(token, gameName);
                response.status(200); // Spec says 200 for success
                CreateGameResult result = new CreateGameResult(gameID);
                String jsonResult = gson.toJson(result);
                return jsonResult;
            }

            if (path.equals("/game") && method.equals("PUT")) {
                // Join a game! PUT /game
                String token = request.headers("authorization"); // Spec says header "authorization"
                JoinGameBody body = gson.fromJson(request.body(), JoinGameBody.class);
                String playerColor = body.playerColor;
                Integer gameID = body.gameID;
                gameService.joinGame(token, playerColor, gameID);
                response.status(200); // Spec says 200 for success
                String emptyJson = "{}";
                return emptyJson;
            }

            // Wrong path or method
            response.status(200);
            String errorMsg = "Error: not found"; // Not in spec but keeping for safety
            ErrorMessage error = new ErrorMessage(errorMsg);
            String jsonError = gson.toJson(error);
            return jsonError;

        } catch (DataAccessException e) {
            // Handle errors per spec
            String msg = e.getMessage();
            if (msg.equals("bad request")) {
                response.status(400);
                String errorMsg = "Error: bad request"; // Spec exact message
                ErrorMessage error = new ErrorMessage(errorMsg);
                String jsonError = gson.toJson(error);
                return jsonError;
            }
            if (msg.equals("unauthorized")) {
                response.status(401);
                String errorMsg = "Error: unauthorized"; // Spec exact message
                ErrorMessage error = new ErrorMessage(errorMsg);
                String jsonError = gson.toJson(error);
                return jsonError;
            }
            if (msg.equals("already taken")) {
                response.status(403);
                String errorMsg = "Error: already taken"; // Spec exact message
                ErrorMessage error = new ErrorMessage(errorMsg);
                String jsonError = gson.toJson(error);
                return jsonError;
            }
            response.status(500);
            String errorMsg = "Error: " + msg; // Spec says description of error
            ErrorMessage error = new ErrorMessage(errorMsg);
            String jsonError = gson.toJson(error);
            return jsonError;
        } catch (Exception e) {
            response.status(500);
            String errorMsg = "Error: " + e.getMessage(); // Spec says description of error
            ErrorMessage error = new ErrorMessage(errorMsg);
            String jsonError = gson.toJson(error);
            return jsonError;
        }
    }

    record ErrorMessage(String message) {}
    record GameListResult(List<GameData> games) {}
    record CreateGameBody(String gameName) {}
    record CreateGameResult(int gameID) {}
    record JoinGameBody(String playerColor, Integer gameID) {}
}