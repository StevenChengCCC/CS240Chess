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
        Spark.staticFiles.location("web");  // if using a web interface

        // Handlers
        ClearHandler clearHandler = new ClearHandler();
        UserHandler userHandler   = new UserHandler(userDAO, authDAO);
        GameHandler gameHandler   = new GameHandler(authDAO, gameDAO);

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
