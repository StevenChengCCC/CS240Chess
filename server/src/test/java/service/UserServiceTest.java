package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeAll
    static void initDatabase() throws DataAccessException {
        DatabaseInitializer.initialize(); // Initialize MySQL database and tables
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        // Initialize MySQL DAO instances
        userDAO = new MySQLUserDAO();
        authDAO = new MySQLAuthDAO();

        // Clear all data before each test
        ClearService clearService = new ClearService(authDAO, userDAO, new MySQLGameDAO());
        clearService.clear();

        userService = new UserService(userDAO, authDAO);
    }

    @AfterEach
    void tearDown() throws DataAccessException {
        ClearService clearService = new ClearService(authDAO, userDAO, new MySQLGameDAO());
        clearService.clear(); // Clean up after each test
    }

    // REGISTER TESTS

    @Test
    @DisplayName("Register Positive: Valid username/password")
    void testRegisterSuccess() throws DataAccessException {
        AuthData auth = userService.register("Steven", "mypassword", "alice@abc.com");
        assertNotNull(auth, "AuthData should not be null");
        assertEquals("Steven", auth.username(), "Username should match");
        assertNotNull(auth.authToken(), "Auth token should not be null");
    }

    @Test
    @DisplayName("Register Negative: Username already taken")
    void testRegisterFailUserExists() throws DataAccessException {
        userService.register("bob", "bobspassword", "bob@xyz.com");
        assertThrows(DataAccessException.class, () -> userService.register("bob", "someOtherPass", "bob2@xyz.com"));
    }

    @Test
    @DisplayName("Register Negative: Missing username")
    void testRegisterFailMissingUsername() {
        assertThrows(DataAccessException.class, () -> userService.register(null, "someramdompassword", "neil@xyz.com"));
    }

    @Test
    @DisplayName("Register Negative: Missing password")
    void testRegisterFailMissingPassword() {
        assertThrows(DataAccessException.class, () -> userService.register("charliewang", null, "charlie@xyz.com"));
    }

    // LOGIN TESTS

    @Test
    @DisplayName("Login Positive: Valid credentials")
    void testLoginSuccess() throws DataAccessException {
        userService.register("david", "davidspass", null);
        AuthData auth = userService.login("david", "davidspass");
        assertNotNull(auth, "AuthData should not be null");
        assertEquals("david", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    @DisplayName("Login Negative: Wrong password")
    void testLoginFailWrongPassword() throws DataAccessException {
        userService.register("maniubi", "niubi", null);
        assertThrows(DataAccessException.class, () -> userService.login("maniubi", "incorrect"));
    }

    @Test
    @DisplayName("Login Negative: Username does not exist")
    void testLoginFailNoSuchUser() {
        assertThrows(DataAccessException.class, () -> userService.login("nonexistent", "nopass"));
    }

    // LOGOUT TESTS

    @Test
    @DisplayName("Logout Positive: Token is valid")
    void testLogoutSuccess() throws DataAccessException {
        AuthData auth = userService.register("bestfriend", "friendspass", null);
        userService.logout(auth.authToken());
        AuthData shouldBeNull = authDAO.getAuth(auth.authToken());
        assertNull(shouldBeNull, "Auth token should be removed");
    }

    @Test
    @DisplayName("Logout Negative: Missing token")
    void testLogoutFailMissingToken() {
        assertThrows(DataAccessException.class, () -> userService.logout(null));
    }

    @Test
    @DisplayName("Logout Negative: Invalid token")
    void testLogoutFailInvalidToken() {
        assertThrows(DataAccessException.class, () -> userService.logout("jhdlsfkjghkfjasdfk"));
    }
}