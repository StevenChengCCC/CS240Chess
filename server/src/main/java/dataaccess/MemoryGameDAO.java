package dataaccess;

import model.GameData;
import java.util.ArrayList;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    @Override
    public void createGame(GameData game) throws DataAccessException {
        if (MemoryDatabase.GAMES.containsKey(game.gameID())) {
            throw new DataAccessException("Game already exists for ID " + game.gameID());
        }
        MemoryDatabase.GAMES.put(game.gameID(), game);
    }
    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return MemoryDatabase.GAMES.get(gameID); //null if not found
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(MemoryDatabase.GAMES.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!MemoryDatabase.GAMES.containsKey(game.gameID())) {
            throw new DataAccessException("Game does not exist for ID " + game.gameID());
        }
        MemoryDatabase.GAMES.put(game.gameID(), game);
    }
}
