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

    @BeforeEach
    void setUp() throws DataAccessException {
        // Clear all data before each test
        MemoryDatabase.clearAll();

        // Initialize the in-memory DAO implementations
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();

        // Create the UserService using these DAO instances
        userService = new UserService(userDAO, authDAO);
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
    void testRegisterFail_UserExists() throws DataAccessException {
        // First registration is successful
        userService.register("bob", "bobspassword", "bob@xyz.com");

        // Second registration with same username => should fail
        assertThrows(DataAccessException.class, () -> {
            userService.register("bob", "someOtherPass", "bob2@xyz.com");
        });
    }


    @Test
    @DisplayName("Register Negative: Missing username")
    void testRegisterFail_MissingUsername() {
        // Expect an exception because the username is null
        assertThrows(DataAccessException.class, () -> {
            userService.register(null, "someramdompassword", "neil@xyz.com");
        });
    }

    @Test
    @DisplayName("Register Negative: Missing password")
    void testRegisterFail_MissingPassword() {
        // Expect an exception because the password is null 王华
        assertThrows(DataAccessException.class, () -> {
            userService.register("charliewang", null, "charlie@xyz.com");
        });
    }

    // LOGIN TESTS

    @Test
    @DisplayName("Login Positive: Valid credentials")
    void testLoginSuccess() throws DataAccessException {
        // first register
        userService.register("david", "davidspass", null);

        // now login
        AuthData auth = userService.login("david", "davidspass");
        assertNotNull(auth, "AuthData should not be null");
        assertEquals("david", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    @DisplayName("Login Negative: Wrong password")
    void testLoginFail_WrongPassword() throws DataAccessException {
        // register
        userService.register("maniubi", "niubi", null);

        // attempt login with wrong password
        assertThrows(DataAccessException.class, () -> {
            userService.login("maniubi", "incorrect");
        });
    }

    @Test
    @DisplayName("Login Negative: Username does not exist")
    void testLoginFail_NoSuchUser() {
        // user not registered
        assertThrows(DataAccessException.class, () -> {
            userService.login("nonexistent", "nopass");
        });
    }

    // LOGOUT TESTS

    @Test
    @DisplayName("Logout Positive: Token is valid")
    void testLogoutSuccess() throws DataAccessException {
        // Register => get token
        AuthData auth = userService.register("bestfriend", "friendspass", null);

        // Logout with that token
        userService.logout(auth.authToken());

        // Confirm that token no longer exists
        AuthData shouldBeNull = authDAO.getAuth(auth.authToken());
        assertNull(shouldBeNull, "Auth token should be removed");
    }

    @Test
    @DisplayName("Logout Negative: Missing token")
    void testLogoutFail_MissingToken() {
        // token is null => throws exception
        assertThrows(DataAccessException.class, () -> userService.logout(null));
    }

    @Test
    @DisplayName("Logout Negative: Invalid token")
    void testLogoutFail_InvalidToken() {
        // try logging out with a made-up token
        assertThrows(DataAccessException.class, () -> userService.logout("jhdlsfkjghkfjasdfk"));
    }
}
