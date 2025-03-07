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
    // Our data access objects and random number maker
    private AuthDAO myAuthDAO;
    private GameDAO myGameDAO;
    private Random myRandom = new Random();

    // Constructor to set up the DAOs
    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.myAuthDAO = authDAO;
        this.myGameDAO = gameDAO;
    }

    // List all games
    public List<GameData> listGames(String token) throws DataAccessException {
        // Check if token is missing
        if (token == null) {
            throw new DataAccessException("unauthorized");
        }

        // Look for the token in the database
        AuthData authStuff = myAuthDAO.getAuth(token);
        // Check if token is valid
        if (authStuff == null) {
            throw new DataAccessException("unauthorized");
        }

        // Get the list of games
        List<GameData> allGames = myGameDAO.listGames();
        // Return the games
        return allGames;
    }

    // Create a new game
    public int createGame(String token, String gameName) throws DataAccessException {
        // Check if token is missing
        if (token == null) {
            throw new DataAccessException("unauthorized");
        }

        // Look for the token in the database
        AuthData authStuff = myAuthDAO.getAuth(token);
        // Check if token is valid
        if (authStuff == null) {
            throw new DataAccessException("unauthorized");
        }

        // Check if game name is missing or empty
        if (gameName == null) {
            throw new DataAccessException("bad request");
        }
        if (gameName.isBlank()) {
            throw new DataAccessException("bad request");
        }

        // Make a random game ID between 1000 and 9999
        int newGameID = 1000 + myRandom.nextInt(9000);
        // Check if this ID is already used
        GameData checkGame = myGameDAO.getGame(newGameID);
        while (checkGame != null) {
            newGameID = 1000 + myRandom.nextInt(9000);
            checkGame = myGameDAO.getGame(newGameID);
        }

        // Make a new chess game
        ChessGame newChessGame = new ChessGame();
        GameData newGame = new GameData(newGameID, null, null, gameName, newChessGame);
        // Save the game in the database
        myGameDAO.createGame(newGame);
        return newGameID;
    }

    // Join a game
    public void joinGame(String token, String playerColor, Integer gameID) throws DataAccessException {
        // Check if token is missing
        if (token == null) {
            throw new DataAccessException("unauthorized");
        }

        // Look for the token in the database
        AuthData authStuff = myAuthDAO.getAuth(token);
        // Check if token is valid
        if (authStuff == null) {
            throw new DataAccessException("unauthorized");
        }

        // Check if color or game ID is missing
        if (playerColor == null) {
            throw new DataAccessException("bad request");
        }
        if (gameID == null) {
            throw new DataAccessException("bad request");
        }

        // Look for the game in the database
        GameData foundGame = myGameDAO.getGame(gameID);
        // Check if game exists
        if (foundGame == null) {
            throw new DataAccessException("bad request");
        }

        // Make the color uppercase to check it
        String upperColor = playerColor.toUpperCase();
        String username = authStuff.username();

        // If player wants to be white
        if (upperColor.equals("WHITE")) {
            String whitePlayer = foundGame.whiteUsername();
            if (whitePlayer != null) {
                throw new DataAccessException("already taken");
            }
            // Make a new game object with the player as white
            GameData updatedGame = new GameData(gameID, username, foundGame.blackUsername(), foundGame.gameName(), foundGame.game());
            myGameDAO.updateGame(updatedGame);
        }
        // If player wants to be black
        else if (upperColor.equals("BLACK")) {
            String blackPlayer = foundGame.blackUsername();
            if (blackPlayer != null) {
                throw new DataAccessException("already taken");
            }
            // Make a new game object with the player as black
            GameData updatedGame = new GameData(gameID, foundGame.whiteUsername(), username, foundGame.gameName(), foundGame.game());
            myGameDAO.updateGame(updatedGame);
        }
        // If color is not white or black
        else {
            throw new DataAccessException("bad request");
        }
    }
}