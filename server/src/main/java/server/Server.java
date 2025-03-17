package server;

import spark.Spark;
import dataaccess.*;
import service.ClearService;

public class Server {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public Server() {
        try {
            DatabaseInitializer.initialize();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage());
        }
        this.userDAO = new MySQLUserDAO();
        this.authDAO = new MySQLAuthDAO();
        this.gameDAO = new MySQLGameDAO();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Create a ClearService instance
        ClearService clearService = new ClearService(authDAO, userDAO, gameDAO);

        // Pass clearService to ClearHandler
        ClearHandler clearHandler = new ClearHandler(clearService);
        UserHandler userHandler = new UserHandler(userDAO, authDAO);
        GameHandler gameHandler = new GameHandler(authDAO, gameDAO);
        // /db => DELETE
        Spark.delete("/db", clearHandler);

        // /user => POST (register)
        // /session => POST (login), DELETE (logout)
        Spark.post("/user", userHandler);
        Spark.post("/session", userHandler);
        Spark.delete("/session", userHandler);

        // /game => GET (list), POST (create), PUT (join)
        Spark.get("/game", gameHandler);
        Spark.post("/game", gameHandler);
        Spark.put("/game", gameHandler);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
    }
}