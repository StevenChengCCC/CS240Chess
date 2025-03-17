package dataaccess;

import model.GameData;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;


public class MySQLGameDAO implements GameDAO{
    private final Gson gson = new Gson();
    @Override
    public void createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (game_id, white_username, black_username, game_name, game_state) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, game.gameID());
            statement.setString(2, game.whiteUsername());
            statement.setString(3, game.blackUsername());
            statement.setString(4, game.gameName());
            statement.setString(5, gson.toJson(game.game()));
            statement.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }
}