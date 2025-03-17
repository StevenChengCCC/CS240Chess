package dataaccess;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import static org.junit.jupiter.api.Assertions.*;

public class MySQLUserDAOTest {
    private MySQLUserDAO userDAO;

    @BeforeAll
    static void init() throws DataAccessException {
        DatabaseInitializer.initialize();
    }

    @BeforeEach
    void setup() throws DataAccessException {
        userDAO = new MySQLUserDAO();
        userDAO.clear();
    }

    @Test
    void testCreateUserPositive() throws DataAccessException {
        UserData user = new UserData("testuser", "password", "test@example.com");
        userDAO.createUser(user);
        UserData retrieved = userDAO.getUser("testuser");
        assertNotNull(retrieved);
        assertEquals("testuser", retrieved.username());
        assertEquals("test@example.com", retrieved.email());
        assertTrue(BCrypt.checkpw("password", retrieved.password())); // Verify password hashing
    }

    @Test
    void testCreateUserNegative() throws DataAccessException {
        UserData user1 = new UserData("testuser", "password1", "test1@example.com");
        UserData user2 = new UserData("testuser", "password2", "test2@example.com");
        userDAO.createUser(user1);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(user2),
                "Expected DataAccessException due to duplicate username");
    }

    @Test
    void testGetUserPositive() throws DataAccessException {
        UserData user = new UserData("testuser", "password", "test@example.com");
        userDAO.createUser(user);
        UserData retrieved = userDAO.getUser("testuser");
        assertNotNull(retrieved);
        assertEquals("testuser", retrieved.username());
    }

    @Test
    void testGetUserNegative() throws DataAccessException {
        UserData retrieved = userDAO.getUser("nonexistent");
        assertNull(retrieved, "Expected null for non-existent user");
    }

    @Test
    void testClearPositive() throws DataAccessException {
        UserData user = new UserData("testuser", "password", "test@example.com");
        userDAO.createUser(user);
        userDAO.clear();
        UserData retrieved = userDAO.getUser("testuser");
        assertNull(retrieved, "Expected null after clearing users");
    }
}