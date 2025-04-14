package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import chess.ChessGame;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@WebSocket
public class WebSocketHandler {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final Gson gson = new Gson();
    private final Map<Session, GameSessionInfo> sessionInfoMap = new ConcurrentHashMap<>();
    private final Map<Integer, Boolean> gameOverMap = new ConcurrentHashMap<>();

    private static class GameSessionInfo {
        int gameID;
        String username;

        GameSessionInfo(int gameID, String username) {
            this.gameID = gameID;
            this.username = username;
        }
    }

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {
        System.out.println("Client connected: " + session.getRemoteAddress().getAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Client disconnected: " + reason);
        handleDisconnect(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String commandTypeStr = json.get("commandType").getAsString();
            UserGameCommand.CommandType commandType = UserGameCommand.CommandType.valueOf(commandTypeStr);
            String authToken = json.get("authToken").getAsString();
            int gameID = json.get("gameID").getAsInt();

            switch (commandType) {
                case CONNECT:
                    handleConnect(session, authToken, gameID);
                    break;
                case MAKE_MOVE:
                    //handleMakeMove(session, authToken, gameID, move);
                    break;
                case LEAVE:
                    //handleLeave(session, authToken, gameID);
                    break;
                case RESIGN:
                    //handleResign(session, authToken, gameID);
                    break;
                default:
                    //sendError(session, "Unknown command type");
            }
        } catch (Exception e) {
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }

    private void handleConnect(Session session, String authToken, int gameID) throws IOException {
        AuthData auth = validateAuth(authToken);
        if (auth == null) {
            sendError(session, "Error: Invalid auth token");
            return;
        }
        GameData game = getGame(gameID);
        if (game == null) {
            sendError(session, "Error: Game not found");
            return;
        }
        String username = auth.username();
        String role = determineRole(username, game);
        sessionInfoMap.put(session, new GameSessionInfo(gameID, username));
        sendLoadGame(session, game);
        broadcastNotification(gameID, session, username + " joined the game as " + role);
    }

    private void handleDisconnect(Session session) {
        GameSessionInfo info = sessionInfoMap.get(session);
        if (info != null) {
            try {
                GameData game = getGame(info.gameID);
                if (game != null) {
                    sessionInfoMap.remove(session);
                    broadcastNotification(info.gameID, session, info.username + " left the game");
                }
            } catch (IOException e) {
                System.err.println("Error during disconnect: " + e.getMessage());
            }
        }
    }

    private AuthData validateAuth(String authToken) {
        try {
            return authDAO.getAuth(authToken);
        } catch (DataAccessException e) {
            return null;
        }
    }

    private GameData getGame(int gameID) {
        try {
            return gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            return null;
        }
    }

    private String determineRole(String username, GameData game) {
        if (username.equals(game.whiteUsername())) {
            return "white";
        } else if (username.equals(game.blackUsername())) {
            return "black";
        } else {
            return "observer";
        }
    }

    private boolean isGameOver(int gameID, ChessGame game) {
        return game.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                game.isInCheckmate(ChessGame.TeamColor.BLACK) ||
                game.isInStalemate(ChessGame.TeamColor.WHITE) ||
                game.isInStalemate(ChessGame.TeamColor.BLACK) ||
                gameOverMap.getOrDefault(gameID, false);
    }

    private void sendLoadGame(Session session, GameData gameData) throws IOException {
        LoadGameMessage msg = new LoadGameMessage(gameData.game());
        session.getRemote().sendString(gson.toJson(msg));
    }

    private void sendError(Session session, String message) throws IOException {
        ErrorMessage msg = new ErrorMessage(message);
        session.getRemote().sendString(gson.toJson(msg));
    }

    private void broadcastNotification(int gameID, Session excludeSession, String notification) throws IOException {
        NotificationMessage msg = new NotificationMessage(notification);
        String json = gson.toJson(msg);
        for (Session s : getSessionsInGame(gameID)) {
            if (s != excludeSession) {
                s.getRemote().sendString(json);
            }
        }
    }

    private Set<Session> getSessionsInGame(int gameID) {
        return sessionInfoMap.entrySet().stream()
                .filter(entry -> entry.getValue().gameID == gameID)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    // Server message classes
    public static class LoadGameMessage extends ServerMessage {
        public ChessGame game;

        public LoadGameMessage(ChessGame game) {
            super(ServerMessageType.LOAD_GAME);
            this.game = game;
        }
    }

    public static class NotificationMessage extends ServerMessage {
        public String message;

        public NotificationMessage(String message) {
            super(ServerMessageType.NOTIFICATION);
            this.message = message;
        }
    }

    public static class ErrorMessage extends ServerMessage {
        public String errorMessage;

        public ErrorMessage(String errorMessage) {
            super(ServerMessageType.ERROR);
            this.errorMessage = errorMessage;
        }
    }
}