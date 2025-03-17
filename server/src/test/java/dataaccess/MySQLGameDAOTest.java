package dataaccess;

import model.GameData;
import chess.ChessGame;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class MySQLGameDAOTest {
    private MySQLGameDAO gameDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        gameDAO = new MySQLGameDAO();
        gameDAO.clear(); // Ensure a clean state before each test
    }

    @Test
    void testCreateGamePositive() throws DataAccessException {
        GameData game = new GameData(1, null, null, "testgame", new ChessGame());
        gameDAO.createGame(game);
        GameData retrieved = gameDAO.getGame(1);
        assertNotNull(retrieved);
        assertEquals(1, retrieved.gameID());
        assertEquals("testgame", retrieved.gameName());
    }

    @Test
    void testCreateGameNegative() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "game1", new ChessGame());
        GameData game2 = new GameData(1, null, null, "game2", new ChessGame());
        gameDAO.createGame(game1);
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(game2),
                "Expected DataAccessException due to duplicate gameID");
    }

    @Test
    void testGetGamePositive() throws DataAccessException {
        // Positive: Successfully retrieve an existing game
        GameData game = new GameData(1, null, null, "testgame", new ChessGame());
        gameDAO.createGame(game);
        GameData retrieved = gameDAO.getGame(1);
        assertNotNull(retrieved);
        assertEquals(1, retrieved.gameID());
    }

    @Test
    void testGetGameNegative() throws DataAccessException {
        GameData retrieved = gameDAO.getGame(999);
        assertNull(retrieved, "Expected null for non-existent game");
    }

    @Test
    void testListGamesPositive() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "game1", new ChessGame());
        GameData game2 = new GameData(2, null, null, "game2", new ChessGame());
        gameDAO.createGame(game1);
        gameDAO.createGame(game2);
        List<GameData> games = gameDAO.listGames();
        assertEquals(2, games.size(), "Expected two games in the list");
    }

    @Test
    void testListGamesNegative() throws DataAccessException {
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty(), "Expected empty list when no games exist");
    }

    @Test
    void testUpdateGamePositive() throws DataAccessException {
        UserDAO userDAO = new MySQLUserDAO();
        UserData user = new UserData("whitePlayer", "password", "email@example.com");
        userDAO.createUser(user);
        GameData game = new GameData(1, null, null, "testgame", new ChessGame());
        gameDAO.createGame(game);
        GameData updatedGame = new GameData(1, "whitePlayer", null, "testgame", new ChessGame());
        gameDAO.updateGame(updatedGame);
        GameData retrieved = gameDAO.getGame(1);
        assertEquals("whitePlayer", retrieved.whiteUsername(), "Expected updated whiteUsername");
    }

    @Test
    void testUpdateGameNegative() throws DataAccessException {
        GameData game = new GameData(999, null, null, "ghostgame", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(game),
                "Expected DataAccessException for non-existent game");
    }

    @Test
    void testClearPositive() throws DataAccessException {
        // Positive: Successfully clear all games
        GameData game = new GameData(1, null, null, "testgame", new ChessGame());
        gameDAO.createGame(game);
        gameDAO.clear();
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty(), "Expected empty list after clearing games");
    }
}