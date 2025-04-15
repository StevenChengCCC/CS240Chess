package ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import chess.ChessGame;
import chess.ChessMove;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class WebSocketClient {
    private final ChessClient chessClient;
    private Session session;
    private String authToken;
    private int gameID;
    private final Gson gson = new Gson();

    public WebSocketClient(ChessClient chessClient) {
        this.chessClient = chessClient;
    }

    public void connect(String authToken, int gameID) {
        this.authToken = authToken;
        this.gameID = gameID;
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI("ws://localhost:8080/ws"));
        } catch (Exception e) {
            chessClient.showError("WebSocket connect failed: " + e.getMessage());
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        sendConnectCommand();
    }

    @OnMessage
    public void onMessage(String message) {
        JsonObject json = gson.fromJson(message, JsonObject.class);
        String type = json.get("serverMessageType").getAsString();
        switch (type) {
            case "LOAD_GAME":
                ChessGame game = gson.fromJson(json.get("game"), ChessGame.class);
                chessClient.updateGame(game);
                break;
            case "NOTIFICATION":
                String notice = json.get("message").getAsString();
                chessClient.showNotification(notice);
                break;
            case "ERROR":
                String errorMessage = json.get("errorMessage").getAsString();
                chessClient.showError(errorMessage);
                break;
            default:
                chessClient.showNotification(message);
        }
    }
    private void sendConnectCommand() {
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "CONNECT");
            cmd.addProperty("authToken", authToken);
            cmd.addProperty("gameID", gameID);
            session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException e) {
            chessClient.showError("Failed to send CONNECT command: " + e.getMessage());
        }
    }

    public void sendMakeMoveCommand(ChessMove move) {
        if (session == null || !session.isOpen()) {
            chessClient.showError("Not connected to server.");
            return;
        }
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "MAKE_MOVE");
            cmd.addProperty("authToken", authToken);
            cmd.addProperty("gameID", gameID);
            JsonObject moveObj = new JsonObject();
            JsonObject startObj = new JsonObject();
            startObj.addProperty("row", move.getStartPosition().getRow());
            startObj.addProperty("col", move.getStartPosition().getColumn());
            JsonObject endObj = new JsonObject();
            endObj.addProperty("row", move.getEndPosition().getRow());
            endObj.addProperty("col", move.getEndPosition().getColumn());
            moveObj.add("startPosition", startObj);
            moveObj.add("endPosition", endObj);
            cmd.add("move", moveObj);
            session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException e) {
            chessClient.showError("Error sending MAKE_MOVE command: " + e.getMessage());
        }
    }

    public void sendResignCommand() {
        if (session == null || !session.isOpen()) {
            chessClient.showError("Not connected to server.");
            return;
        }
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "RESIGN");
            cmd.addProperty("authToken", authToken);
            cmd.addProperty("gameID", gameID);
            session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException e) {
            chessClient.showError("Error sending RESIGN command: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                JsonObject cmd = new JsonObject();
                cmd.addProperty("commandType", "LEAVE");
                cmd.addProperty("authToken", authToken);
                cmd.addProperty("gameID", gameID);
                session.getBasicRemote().sendText(gson.toJson(cmd));
                session.close();
            } catch (IOException e) {
                chessClient.showError("Error leaving game: " + e.getMessage());
            }
        }
    }
}