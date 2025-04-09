package websocket.messages;

import chess.ChessGame;

public class ServerMessage {
    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    private final ServerMessageType serverMessageType;
    private ChessGame game; // For LOAD_GAME
    private String errorMessage; // For ERROR
    private String message; // For NOTIFICATION

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessage(ServerMessageType type, ChessGame game) {
        this(type);
        this.game = game;
    }

    public ServerMessage(ServerMessageType type, String message) {
        this(type);
        if (type == ServerMessageType.ERROR) {
            this.errorMessage = message;
        } else if (type == ServerMessageType.NOTIFICATION) {
            this.message = message;
        }
    }

    public ServerMessageType getServerMessageType() {
        return serverMessageType;
    }

    public ChessGame getGame() {
        return game;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getMessage() {
        return message;
    }
}