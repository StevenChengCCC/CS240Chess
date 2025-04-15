package websocket.commands;

import chess.ChessMove;

public class UserGameCommand {
    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    private CommandType commandType;
    private String authToken;
    private int gameID;
    private ChessMove move;

    public CommandType getCommandType() {
        return commandType;
    }
    public String getAuthToken() {
        return authToken;
    }
    public int getGameID() {
        return gameID;
    }
    public ChessMove getMove() {
        return move;
    }

    // Optionally: Constructors / setters if needed
    public UserGameCommand() {}
}
