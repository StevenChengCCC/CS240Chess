package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService gameService;
    private UserService userService;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    @BeforeAll
    static void initDatabase() throws DataAccessException {
        DatabaseInitializer.initialize(); // Initialize MySQL database and tables
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        // Initialize MySQL DAO instances
        authDAO = new MySQLAuthDAO();
        gameDAO = new MySQLGameDAO();
        userDAO = new MySQLUserDAO();

        // Clear all data before each test
        ClearService clearService = new ClearService(authDAO, userDAO, gameDAO);
        clearService.clear();

        gameService = new GameService(authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
    }

    @AfterEach
    void tearDown() throws DataAccessException {
        ClearService clearService = new ClearService(authDAO, userDAO, gameDAO);
        clearService.clear(); // Clean up after each test
    }

    // LIST GAMES TESTS

    @Test
    @DisplayName("ListGames Positive: Valid token returns empty list initially")
    void testListGamesSuccessEmpty() throws DataAccessException {
        AuthData auth = userService.register("alice", "pass", null);
        List<GameData> games = gameService.listGames(auth.authToken());
        assertNotNull(games);
        assertEquals(0, games.size(), "Initially, no games should exist");
    }

    @Test
    @DisplayName("ListGames Negative: Null token")
    void testListGamesFailNullToken() {
        assertThrows(DataAccessException.class, () -> gameService.listGames(null));
    }

    @Test
    @DisplayName("ListGames Negative: Invalid token")
    void testListGamesFailInvalidToken() {
        assertThrows(DataAccessException.class, () -> gameService.listGames("bogus_token_555"));
    }

    // CREATE GAME TESTS

    @Test
    @DisplayName("CreateGame Positive: Valid token, valid game name")
    void testCreateGameSuccess() throws DataAccessException {
        AuthData auth = userService.register("bob", "bobpass", null);
        int gameID = gameService.createGame(auth.authToken(), "MyTestGame");
        assertTrue(gameID > 0, "Game ID should be positive"); // MySQL typically uses auto-increment IDs

        GameData retrieved = gameDAO.getGame(gameID);
        assertNotNull(retrieved, "Newly created game should exist in DB");
        assertEquals("MyTestGame", retrieved.gameName());
        assertNull(retrieved.whiteUsername(), "No white player yet");
        assertNull(retrieved.blackUsername(), "No black player yet");
    }

    @Test
    @DisplayName("CreateGame Negative: Missing token")
    void testCreateGameFailMissingToken() {
        assertThrows(DataAccessException.class, () -> gameService.createGame(null, "NoTokenGame"));
    }

    @Test
    @DisplayName("CreateGame Negative: Invalid token")
    void testCreateGameFailInvalidToken() {
        assertThrows(DataAccessException.class, () -> gameService.createGame("invalid_token", "AnotherGame"));
    }

    @Test
    @DisplayName("CreateGame Negative: Blank game name")
    void testCreateGameFailBlankGameName() throws DataAccessException {
        AuthData auth = userService.register("steven", "carolpass", null);
        assertThrows(DataAccessException.class, () -> gameService.createGame(auth.authToken(), "   "));
    }

    // JOIN GAME TESTS

    @Test
    @DisplayName("JoinGame Positive: Valid token, color, game")
    void testJoinGameSuccess() throws DataAccessException {
        AuthData auth = userService.register("stephen", "password", null);
        int gameID = gameService.createGame(auth.authToken(), "Joinable Game");
        gameService.joinGame(auth.authToken(), "WHITE", gameID);

        GameData joined = gameDAO.getGame(gameID);
        assertNotNull(joined);
        assertEquals("stephen", joined.whiteUsername());
        assertNull(joined.blackUsername());
    }

    @Test
    @DisplayName("JoinGame Negative: Missing token")
    void testJoinGameFailMissingToken() {
        assertThrows(DataAccessException.class, () -> gameService.joinGame(null, "WHITE", 1001));
    }

    @Test
    @DisplayName("JoinGame Negative: Invalid color")
    void testJoinGameFailInvalidColor() throws DataAccessException {
        AuthData auth = userService.register("david", "ajdfsfakljfhasd", null);
        int gameID = gameService.createGame(auth.authToken(), "BadColorGame");
        assertThrows(DataAccessException.class, () -> gameService.joinGame(auth.authToken(), "BLUE", gameID));
    }

    @Test
    @DisplayName("JoinGame Negative: Already taken color")
    void testJoinGameFailColorTaken() throws DataAccessException {
        AuthData auth1 = userService.register("steven1", "password1", null);
        AuthData auth2 = userService.register("steven2", "password2", null);
        int gameID = gameService.createGame(auth1.authToken(), "JoinTestGame");
        gameService.joinGame(auth1.authToken(), "WHITE", gameID);
        assertThrows(DataAccessException.class, () -> gameService.joinGame(auth2.authToken(), "WHITE", gameID));
    }

    @Test
    @DisplayName("JoinGame Negative: Nonexistent game")
    void testJoinGameFailNonexistentGame() throws DataAccessException {
        AuthData auth = userService.register("steven3", "password3", null);
        assertThrows(DataAccessException.class, () -> gameService.joinGame(auth.authToken(), "WHITE", 9999));
    }
}