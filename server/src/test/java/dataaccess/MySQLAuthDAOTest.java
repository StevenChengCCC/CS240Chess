package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class MySQLAuthDAOTest {
    private MySQLUserDAO userDAO;
    private MySQLAuthDAO authDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        userDAO = new MySQLUserDAO();
        authDAO = new MySQLAuthDAO();
        userDAO.clear();
        authDAO.clear();
    }

    @Test
    void testCreateAuthPositive() throws DataAccessException {
        UserData user = new UserData("testuser", "password", "test@example.com");
        userDAO.createUser(user);
        AuthData auth = new AuthData("token1", "testuser");
        authDAO.createAuth(auth);
        AuthData retrieved = authDAO.getAuth("token1");
        assertNotNull(retrieved);
        assertEquals("token1", retrieved.authToken());
        assertEquals("testuser", retrieved.username());
    }

    @Test
    void testCreateAuthNegative() throws DataAccessException {
        UserData user = new UserData("testuser", "password", "test@example.com");
        userDAO.createUser(user);
        AuthData auth1 = new AuthData("token1", "testuser");
        AuthData auth2 = new AuthData("token1", "testuser");
        authDAO.createAuth(auth1);
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth2),
                "Expected DataAccessException due to duplicate auth token");
    }

    @Test
    void testGetAuthPositive() throws DataAccessException {
        UserData user = new UserData("testuser", "password", "test@example.com");
        userDAO.createUser(user);
        AuthData auth = new AuthData("token1", "testuser");
        authDAO.createAuth(auth);
        AuthData retrieved = authDAO.getAuth("token1");
        assertNotNull(retrieved);
        assertEquals("token1", retrieved.authToken());
    }

    @Test
    void testGetAuthNegative() throws DataAccessException {
        AuthData retrieved = authDAO.getAuth("nonexistent");
        assertNull(retrieved, "Expected null for non-existent auth token");
    }

    @Test
    void testDeleteAuthPositive() throws DataAccessException {
        UserData user = new UserData("testuser", "password", "test@example.com");
        userDAO.createUser(user);
        AuthData auth = new AuthData("token1", "testuser");
        authDAO.createAuth(auth);
        authDAO.deleteAuth("token1");
        AuthData retrieved = authDAO.getAuth("token1");
        assertNull(retrieved, "Expected null after deleting auth token");
    }

    @Test
    void testDeleteAuthNegative() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> authDAO.deleteAuth("nonexistent"),
                "Expected DataAccessException for non-existent auth token");
    }

    @Test
    void testClearPositive() throws DataAccessException {
        UserData user = new UserData("testuser", "password", "test@example.com");
        userDAO.createUser(user);
        AuthData auth = new AuthData("token1", "testuser");
        authDAO.createAuth(auth);
        authDAO.clear();
        AuthData retrieved = authDAO.getAuth("token1");
        assertNull(retrieved, "Expected null after clearing auths");
    }
}