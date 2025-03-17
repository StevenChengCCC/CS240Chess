package dataaccess;

import model.GameData;
import java.util.List;

public interface GameDAO {
    // Create a new game
    void createGame(GameData game) throws DataAccessException;

    // Retrieve a game by its ID
    GameData getGame(int gameID) throws DataAccessException;

    // Returns all games
    List<GameData> listGames() throws DataAccessException;

    // Update an existing game
    void updateGame(GameData game) throws DataAccessException;

    void clear() throws DataAccessException;
}
