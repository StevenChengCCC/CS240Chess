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

    @BeforeAll
    static void initDatabase() throws DataAccessException {
        DatabaseInitializer.initialize(); // Initialize MySQL database and tables
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        // Initialize MySQL DAO instances
        userDAO = new MySQLUserDAO();
        authDAO = new MySQLAuthDAO();
        gameDAO = new MySQLGameDAO();

        clearService = new ClearService(authDAO, userDAO, gameDAO);
        clearService.clear(); // Clear all data before each test
    }

    @AfterEach
    void tearDown() throws DataAccessException {
        clearService.clear(); // Clear all data after each test
    }

    @Test
    @DisplayName("ClearService: All data removed")
    void testClearService() throws DataAccessException {
        // Insert some dummy data
        userDAO.createUser(new UserData("steven", "password", "steven@gmail.com"));
        authDAO.createAuth(new AuthData("absdfasdkfakshjdlf", "steven"));
        gameDAO.createGame(new GameData(8888, null, null, "test game", null));

        // Confirm data is in the database
        assertNotNull(userDAO.getUser("steven"));
        assertNotNull(authDAO.getAuth("absdfasdkfakshjdlf"));
        assertNotNull(gameDAO.getGame(8888));

        // Now clear
        clearService.clear();

        // Confirm data is removed
        assertNull(userDAO.getUser("steven"), "User should be cleared");
        assertNull(authDAO.getAuth("absdfasdkfakshjdlf"), "Auth token should be cleared");
        assertNull(gameDAO.getGame(8888), "Game should be cleared"); // Fixed game ID to match inserted data
    }
}