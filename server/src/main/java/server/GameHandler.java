package server;

import spark.Request;
import spark.Response;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import service.GameService;
import model.GameData;
import java.util.List;

public class GameHandler extends BaseHandler {
    private final GameService gameService;

    public GameHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.gameService = new GameService(authDAO, gameDAO);
    }

    @Override
    protected Object handleRequest(Request request, Response response) throws DataAccessException {
        String path = request.pathInfo();
        String method = request.requestMethod();

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
    }

    @Override
    protected String getAlreadyTakenMessage() {
        return "Error: player color already taken";
    }

    record GameListResult(List<GameData> games) {}
    record CreateGameBody(String gameName) {}
    record CreateGameResult(int gameID) {}
    record JoinGameBody(String playerColor, Integer gameID) {}
}