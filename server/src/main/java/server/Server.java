package server;

import spark.Spark;
import dataaccess.*;

public class Server {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public Server() {
        // Create memory-based DAO objects
        this.userDAO = new MemoryUserDAO();
        this.authDAO = new MemoryAuthDAO();
        this.gameDAO = new MemoryGameDAO();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Handlers
        AuthHandler clearHandler = new AuthHandler();
        UserHandler userHandler   = new UserHandler(userDAO, authDAO);
        GameHandler gameHandler   = new GameHandler(authDAO, gameDAO);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }
}
