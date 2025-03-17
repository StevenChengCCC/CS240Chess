package dataaccess;

import model.GameData;
import chess.ChessGame;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MySQLGameDAO implements GameDAO {
    private final Gson gson = new Gson();

    @Override
    public void createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (game_id, white_username, black_username, game_name, game_data) VALUES (?, ?, ?, ?, ?)";
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
        String sql = "SELECT game_id, white_username, black_username, game_name, game_data FROM games WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, gameID);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int dbGameID = rs.getInt("game_id");
                String whiteUsername = rs.getString("white_username");
                String blackUsername = rs.getString("black_username");
                String gameName = rs.getString("game_name");
                String gameStateJson = rs.getString("game_data");
                ChessGame gameState = gson.fromJson(gameStateJson, ChessGame.class);
                return new GameData(dbGameID, whiteUsername, blackUsername, gameName, gameState);
            }
            return null;
        } catch (Exception e) {
            throw new DataAccessException("Error getting game: " + e.getMessage());
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        String sql = "SELECT game_id, white_username, black_username, game_name, game_data FROM games";
        List<GameData> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                int gameID = rs.getInt("game_id");
                String whiteUsername = rs.getString("white_username");
                String blackUsername = rs.getString("black_username");
                String gameName = rs.getString("game_name");
                String gameStateJson = rs.getString("game_data");
                ChessGame gameState = gson.fromJson(gameStateJson, ChessGame.class);
                games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, gameState));
            }
            return games;
        } catch (Exception e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET white_username = ?, black_username = ?, game_name = ?, game_data = ? WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, game.whiteUsername());
            statement.setString(2, game.blackUsername());
            statement.setString(3, game.gameName());
            statement.setString(4, gson.toJson(game.game()));
            statement.setInt(5, game.gameID());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Game not found");
            }
        } catch (Exception e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage());
        }
    }
}