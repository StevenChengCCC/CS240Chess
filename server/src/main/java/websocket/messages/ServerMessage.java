package websocket.messages;

import model.GameData;

public class ServerMessage {
    public enum ServerMessageType { LOAD_GAME, NOTIFICATION, ERROR }

    private final ServerMessageType serverMessageType;
    private final GameData game; // For LOAD_GAME
    private final String message; // For NOTIFICATION and ERROR

    public ServerMessage(ServerMessageType serverMessageType, GameData game, String message) {
        this.serverMessageType = serverMessageType;
        this.game = game;
        this.message = message;
    }

    public ServerMessageType getServerMessageType() { return serverMessageType; }
    public GameData getGame() { return game; }
    public String getMessage() { return message; }
}