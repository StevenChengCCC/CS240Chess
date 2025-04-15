package ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

import chess.ChessGame;
import model.GameData;
import chess.ChessPosition;

@ClientEndpoint
public class WebSocketClient {

    private Session session;
    private final Gson gson = new Gson();
    private final ChessClient chessClient;

    public WebSocketClient(ChessClient chessClient) {
        this.chessClient = chessClient;
    }

    /**
     * Call this to open a WebSocket connection and send CONNECT
     */
    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            // Adjust host/port/endpoint as needed
            container.connectToServer(this, new URI("ws://localhost:8080/ws"));
        } catch (Exception e) {
            System.out.println("WebSocket connect failed: " + e.getMessage());
        }
    }

    /**
     * True if the WebSocket session is open
     */
    public boolean isSessionOpen() {
        return (session != null && session.isOpen());
    }

    // ------------------------------
    // WebSocket lifecycle callbacks
    // ------------------------------

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        if (chessClient.getAuthData() != null && chessClient.getCurrentGame() != null) {
            sendConnectCommand();
        }
    }

    @OnMessage
    public void onMessage(String message) {
        JsonObject json = gson.fromJson(message, JsonObject.class);
        String type = json.get("serverMessageType").getAsString();

        switch (type) {
            case "LOAD_GAME" -> {
                ChessGame receivedGame = gson.fromJson(json.get("game"), ChessGame.class);
                GameData currentGame = chessClient.getCurrentGame();
                if (currentGame == null) {
                    // If there's no currentGame yet, create a minimal placeholder
                    currentGame = new GameData(-1, null, null, null, receivedGame);
                } else {
                    // Update the existing currentGame object with the new ChessGame
                    currentGame = new GameData(
                            currentGame.gameID(),
                            currentGame.whiteUsername(),
                            currentGame.blackUsername(),
                            currentGame.gameName(),
                            receivedGame
                    );
                }
                chessClient.updateGameState(currentGame);
            }
            case "NOTIFICATION" -> {
                String notice = json.get("message").getAsString();
                System.out.println(notice);
            }
            case "ERROR" -> {
                String errorMessage = json.get("errorMessage").getAsString();
                System.out.println(errorMessage);
            }
            default -> {
                System.out.println("Received: " + message);
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
        System.out.println("WebSocket closed: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable t) {
        System.out.println("WebSocket error: " + t.getMessage());
    }

    // ------------------------------
    // Sending commands to the server
    // ------------------------------

    private void sendConnectCommand() {
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "CONNECT");
            cmd.addProperty("authToken", chessClient.getAuthData().authToken());
            cmd.addProperty("gameID", chessClient.getCurrentGame().gameID());
            session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException e) {
            System.out.println("Failed to send CONNECT command: " + e.getMessage());
        }
    }

    public void sendMoveCommand(ChessPosition startPos, ChessPosition endPos) {
        if (!isSessionOpen()) {
            System.out.println("WebSocket session is not open.");
            return;
        }
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "MAKE_MOVE");
            cmd.addProperty("authToken", chessClient.getAuthData().authToken());
            cmd.addProperty("gameID", chessClient.getCurrentGame().gameID());

            JsonObject moveObj = new JsonObject();
            JsonObject startObj = new JsonObject();
            startObj.addProperty("row", startPos.getRow());
            startObj.addProperty("col", startPos.getColumn());
            JsonObject endObj = new JsonObject();
            endObj.addProperty("row", endPos.getRow());
            endObj.addProperty("col", endPos.getColumn());

            moveObj.add("startPosition", startObj);
            moveObj.add("endPosition", endObj);
            cmd.add("move", moveObj);

            session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException e) {
            System.out.println("Error sending MAKE_MOVE command: " + e.getMessage());
        }
    }

    public void sendResignCommand() {
        if (!isSessionOpen()) {
            System.out.println("WebSocket session is not open.");
            return;
        }
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "RESIGN");
            cmd.addProperty("authToken", chessClient.getAuthData().authToken());
            cmd.addProperty("gameID", chessClient.getCurrentGame().gameID());
            session.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException e) {
            System.out.println("Error sending RESIGN command: " + e.getMessage());
        }
    }

    public void sendLeaveCommand() {
        if (!isSessionOpen()) {
            System.out.println("WebSocket session is not open.");
            return;
        }
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "LEAVE");
            cmd.addProperty("authToken", chessClient.getAuthData().authToken());
            cmd.addProperty("gameID", chessClient.getCurrentGame().gameID());
            session.getBasicRemote().sendText(gson.toJson(cmd));

            session.close();
        } catch (IOException e) {
            System.out.println("Error leaving game: " + e.getMessage());
        }
    }
}
