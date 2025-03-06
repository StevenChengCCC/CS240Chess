package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import chess.ChessGame;
import java.util.List;
import java.util.Random;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final Random random = new Random();

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public List<GameData> listGames(String token) throws DataAccessException {
        if (token == null) {
            throw new DataAccessException("unauthorized");
        }
        AuthData auth = authDAO.getAuth(token);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        List<GameData> games = gameDAO.listGames();
        return games;
    }

    public int createGame(String token, String gameName) throws DataAccessException {
        if (token == null) {
            throw new DataAccessException("unauthorized");
        }
        AuthData auth = authDAO.getAuth(token);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        if (gameName == null || gameName.isBlank()) {
            throw new DataAccessException("bad request");
        }

        int gameID = 1000 + random.nextInt(9000);
        GameData checkGame = gameDAO.getGame(gameID);
        while (checkGame != null) {
            gameID = 1000 + random.nextInt(9000);
            checkGame = gameDAO.getGame(gameID);
        }

        ChessGame chessGame = new ChessGame();
        GameData newGame = new GameData(gameID, null, null, gameName, chessGame);
        gameDAO.createGame(newGame);
        return gameID;
    }

    public void joinGame(String token, String playerColor, Integer gameID) throws DataAccessException {
        if (token == null) {
            throw new DataAccessException("unauthorized");
        }
        AuthData auth = authDAO.getAuth(token);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        if (playerColor == null || gameID == null) {
            throw new DataAccessException("bad request");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("bad request");
        }

        String upperColor = playerColor.toUpperCase();
        if (upperColor.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("already taken");
            }
            GameData updatedGame = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
            gameDAO.updateGame(updatedGame);
        }
        if (upperColor.equals("BLACK")) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("already taken");
            }
            GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
            gameDAO.updateGame(updatedGame);
        }
        if (!upperColor.equals("WHITE") && !upperColor.equals("BLACK")) {
            throw new DataAccessException("bad request");
        }
    }
}