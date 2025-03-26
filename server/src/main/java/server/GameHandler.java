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
                String token = request.headers("authorization");
                List<GameData> games = gameService.listGames(token);
                response.status(200);
                GameListResult result = new GameListResult(games);
                return gson.toJson(result);
            }

            if (path.equals("/game") && method.equals("POST")) {
                String token = request.headers("authorization");
                CreateGameBody body = gson.fromJson(request.body(), CreateGameBody.class);
                String gameName = body.gameName;
                int gameID = gameService.createGame(token, gameName);
                response.status(200);
                CreateGameResult result = new CreateGameResult(gameID);
                return gson.toJson(result);
            }

            if (path.equals("/game") && method.equals("PUT")) {
                String token = request.headers("authorization");
                JoinGameBody body = gson.fromJson(request.body(), JoinGameBody.class);
                String playerColor = body.playerColor;
                Integer gameID = body.gameID;
                gameService.joinGame(token, playerColor, gameID);
                response.status(200);
                return "{}";
            }

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
                ErrorMessage error = new ErrorMessage("Error: player color already taken");
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

    record ErrorMessage(String message) {}
    record GameListResult(List<GameData> games) {}
    record CreateGameBody(String gameName) {}
    record CreateGameResult(int gameID) {}
    record JoinGameBody(String playerColor, Integer gameID) {}
}