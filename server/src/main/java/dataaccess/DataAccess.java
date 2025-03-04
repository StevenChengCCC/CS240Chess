package dataaccess;

import model.UserData;
import model.GameData;
import model.AuthData;
import java.util.List;

public interface DataAccess {
    // Clears all data (users, games, auth)
    void clear() throws DataAccessException;

    // Creates and retrieves users
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;

    // Creates and retrieves Auth tokens
    void createAuth(AuthData auth) throws DataAccessException;
    AuthData getAuth(String token) throws DataAccessException;
    void deleteAuth(String token) throws DataAccessException;

    // Creates and retrieves games
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;

    // Update an existing game (e.g., players joining, moves made)
    void updateGame(GameData game) throws DataAccessException;
}
