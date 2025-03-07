package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    private ClearService clearService;
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        MemoryDatabase.clearAll(); // empty it first just to be safe

        // create in-memory DAO instances
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        clearService = new ClearService(authDAO, userDAO, gameDAO);
    }

    @Test
    @DisplayName("ClearService: All data removed")
    void testClearService() throws DataAccessException {
        // Insert some dummy data
        userDAO.createUser(new UserData("steven", "password", "steven@gmail.com"));
        authDAO.createAuth(new AuthData("absdfasdkfakshjdlf", "steven"));
        gameDAO.createGame(new GameData(8888, null, null, "test game", null));

        // Confirm data is in memory
        assertNotNull(userDAO.getUser("steven"));
        assertNotNull(authDAO.getAuth("absdfasdkfakshjdlf"));
        assertNotNull(gameDAO.getGame(8888));

        // Now clear
        clearService.clear();

        // Confirm data is removed
        assertNull(userDAO.getUser("steven"), "User should be cleared");
        assertNull(authDAO.getAuth("absdfasdkfakshjdlf"), "Auth token should be cleared");
        assertNull(gameDAO.getGame(1234), "Game should be cleared");
    }
}
