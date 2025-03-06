package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import chess.ChessGame;

import java.util.List;
import java.util.Random;

//service for listing, creating, and joining a game.
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
            throw new DataAccessException("unauthorized: missing token");
        }
        var auth = authDAO.getAuth(token);
        if (auth == null) {
            throw new DataAccessException("unauthorized: invalid token");
        }
        return gameDAO.listGames();
    }
}