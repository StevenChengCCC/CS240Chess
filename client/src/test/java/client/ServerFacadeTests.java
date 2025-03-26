package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;
import ui.ClientException;
import ui.ServerFacade;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws ClientException {
        facade.sendRequest("DELETE", "/db", null, null);
    }

    // --- Register Tests ---

    @Test
    void registerSuccess() throws ClientException {
        AuthData authData = facade.register("player1", "password", "p1@email.com");
        assertNotNull(authData);
        assertEquals("player1", authData.username());
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerFailureDuplicateUsername() {
        assertDoesNotThrow(() -> facade.register("player1", "password", "p1@email.com"));
        ClientException exception = assertThrows(ClientException.class, () ->
                facade.register("player1", "differentPassword", "p2@email.com"));
        assertTrue(exception.getMessage().contains("Error: username already taken"));
    }

    // --- Login Tests ---

    @Test
    void loginSuccess() throws ClientException {
        facade.register("player1", "password", "p1@email.com");
        AuthData authData = facade.login("player1", "password");
        assertNotNull(authData);
        assertEquals("player1", authData.username());
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void loginFailureWrongPassword() throws ClientException {
        facade.register("player1", "password", "p1@email.com");
        ClientException exception = assertThrows(ClientException.class, () ->
                facade.login("player1", "wrongPassword"));
        assertTrue(exception.getMessage().contains("Error: incorrect password"));
    }

    @Test
    void loginFailureUserNotExist() throws ClientException {
        ClientException exception = assertThrows(ClientException.class, () ->
                facade.login("nonexistent", "password"));
        assertTrue(exception.getMessage().contains("Error: user does not exist"));
    }

    // --- Logout Tests ---

    @Test
    void logoutSuccess() throws ClientException {
        AuthData authData = facade.register("player1", "password", "p1@email.com");
        assertDoesNotThrow(() -> facade.logout(authData.authToken()));
        ClientException exception = assertThrows(ClientException.class, () ->
                facade.listGames(authData.authToken()));
        assertTrue(exception.getMessage().contains("Error: invalid or missing authentication token"));
    }

    @Test
    void logoutFailureInvalidToken() {
        ClientException exception = assertThrows(ClientException.class, () ->
                facade.logout("invalid-token"));
        assertTrue(exception.getMessage().contains("Error: invalid or missing authentication token"));
    }

    // --- CreateGame Tests ---

    @Test
    void createGameSuccess() throws ClientException {
        AuthData authData = facade.register("player1", "password", "p1@email.com");
        int gameID = facade.createGame(authData.authToken(), "TestGame");
        assertTrue(gameID >= 1000 && gameID <= 9999);
        List<GameData> games = facade.listGames(authData.authToken());
        assertEquals(1, games.size());
        assertEquals("TestGame", games.get(0).gameName());
    }

    @Test
    void createGameFailureUnauthorized() {
        ClientException exception = assertThrows(ClientException.class, () ->
                facade.createGame("invalid-token", "TestGame"));
        assertTrue(exception.getMessage().contains("Error: invalid or missing authentication token"));
    }

    // --- ListGames Tests ---

    @Test
    void listGamesSuccess() throws ClientException {
        AuthData authData = facade.register("player1", "password", "p1@email.com");
        facade.createGame(authData.authToken(), "Game1");
        facade.createGame(authData.authToken(), "Game2");
        List<GameData> games = facade.listGames(authData.authToken());
        assertEquals(2, games.size());
        assertTrue(games.stream().anyMatch(game -> game.gameName().equals("Game1")));
        assertTrue(games.stream().anyMatch(game -> game.gameName().equals("Game2")));
    }

    @Test
    void listGamesFailureUnauthorized() {
        ClientException exception = assertThrows(ClientException.class, () ->
                facade.listGames("invalid-token"));
        assertTrue(exception.getMessage().contains("Error: invalid or missing authentication token"));
    }

    // --- JoinGame Tests ---

    @Test
    void joinGameSuccess() throws ClientException {
        AuthData authData = facade.register("player1", "password", "p1@email.com");
        int gameID = facade.createGame(authData.authToken(), "TestGame");
        GameData game = facade.joinGame(authData.authToken(), "white", gameID);
        assertNotNull(game);
        assertEquals(gameID, game.gameID());
        assertEquals("player1", game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    void joinGameFailureAlreadyTaken() throws ClientException {
        AuthData authData1 = facade.register("player1", "password", "p1@email.com");
        AuthData authData2 = facade.register("player2", "password2", "p2@email.com");
        int gameID = facade.createGame(authData1.authToken(), "TestGame");
        facade.joinGame(authData1.authToken(), "white", gameID);
        ClientException exception = assertThrows(ClientException.class, () ->
                facade.joinGame(authData2.authToken(), "white", gameID));
        assertTrue(exception.getMessage().contains("Error: player color already taken"));
    }
}