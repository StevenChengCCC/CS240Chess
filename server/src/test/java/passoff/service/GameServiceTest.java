package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService gameService;
    private UserService userService;  // so we can create valid tokens easily

    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        MemoryDatabase.clearAll();

        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userDAO = new MemoryUserDAO();

        gameService = new GameService(authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
    }

    // LIST GAMES TESTS

    @Test
    @DisplayName("ListGames Positive: Valid token returns empty list initially")
    void testListGamesSuccess_Empty() throws DataAccessException {
        // first register => get token
        AuthData auth = userService.register("alice", "pass", null);

        List<GameData> games = gameService.listGames(auth.authToken());
        assertNotNull(games);
        assertEquals(0, games.size(), "Initially, no games should exist");
    }

    @Test
    @DisplayName("ListGames Negative: Null token")
    void testListGamesFail_NullToken() {
        assertThrows(DataAccessException.class, () -> {
            gameService.listGames(null);
        });
    }

    @Test
    @DisplayName("ListGames Negative: Invalid token")
    void testListGamesFail_InvalidToken() {
        assertThrows(DataAccessException.class, () -> {
            gameService.listGames("bogus_token_555");
        });
    }

    // CREATE GAME TESTS

    @Test
    @DisplayName("CreateGame Positive: Valid token, valid game name")
    void testCreateGameSuccess() throws DataAccessException {
        AuthData auth = userService.register("bob", "bobpass", null);

        int gameID = gameService.createGame(auth.authToken(), "MyTestGame");
        assertTrue(gameID >= 1000 && gameID <= 9999, "Game ID should be between 1000 and 9999");

        GameData retrieved = gameDAO.getGame(gameID);
        assertNotNull(retrieved, "Newly created game should exist in DB");
        assertEquals("MyTestGame", retrieved.gameName());
        assertNull(retrieved.whiteUsername(), "No white player yet");
        assertNull(retrieved.blackUsername(), "No black player yet");
    }

    @Test
    @DisplayName("CreateGame Negative: Missing token")
    void testCreateGameFail_MissingToken() {
        assertThrows(DataAccessException.class, () -> {
            gameService.createGame(null, "NoTokenGame");
        });
    }

    @Test
    @DisplayName("CreateGame Negative: Invalid token")
    void testCreateGameFail_InvalidToken() {
        assertThrows(DataAccessException.class, () -> {
            gameService.createGame("invalid_token", "AnotherGame");
        });
    }

    @Test
    @DisplayName("CreateGame Negative: Blank game name")
    void testCreateGameFail_BlankGameName() throws DataAccessException {
        AuthData auth = userService.register("steven", "carolpass", null);

        assertThrows(DataAccessException.class, () -> {
            gameService.createGame(auth.authToken(), "   ");
        });
    }

    // JOIN GAME TESTS

    @Test
    @DisplayName("JoinGame Positive: Valid token, color, game")
    void testJoinGameSuccess() throws DataAccessException {
        // user to get a token
        AuthData auth = userService.register("stephen", "password", null);
        // create a game
        int gameID = gameService.createGame(auth.authToken(), "Joinable Game");

        // join as white
        gameService.joinGame(auth.authToken(), "WHITE", gameID);

        // check DB
        GameData joined = gameDAO.getGame(gameID);
        assertNotNull(joined);
        assertEquals("stephen", joined.whiteUsername());
        assertNull(joined.blackUsername());
    }

    @Test
    @DisplayName("JoinGame Negative: Missing token")
    void testJoinGameFail_MissingToken() {
        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(null, "WHITE", 1001);
        });
    }

    @Test
    @DisplayName("JoinGame Negative: Invalid color")
    void testJoinGameFail_InvalidColor() throws DataAccessException {
        // user & game
        AuthData auth = userService.register("david", "ajdfsfakljfhasd", null);
        int gameID = gameService.createGame(auth.authToken(), "BadColorGame");

        // attempt to join with invalid color
        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(auth.authToken(), "BLUE", gameID);
        });
    }

    @Test
    @DisplayName("JoinGame Negative: Already taken color")
    void testJoinGameFail_ColorTaken() throws DataAccessException {
        // user1 & user2
        AuthData auth1 = userService.register("steven1", "password1", null);
        AuthData auth2 = userService.register("steven2", "password2", null);

        // create game with first user
        int gameID = gameService.createGame(auth1.authToken(), "JoinTestGame");
        // user1 joins as white
        gameService.joinGame(auth1.authToken(), "WHITE", gameID);

        // user2 attempts to join as white again
        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(auth2.authToken(), "WHITE", gameID);
        });
    }

    @Test
    @DisplayName("JoinGame Negative: Nonexistent game")
    void testJoinGameFail_NonexistentGame() throws DataAccessException {
        AuthData auth = userService.register("steven3", "password3", null);
        // No game was created

        // attempt to join a made-up game ID
        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(auth.authToken(), "WHITE", 9999);
        });
    }
}
