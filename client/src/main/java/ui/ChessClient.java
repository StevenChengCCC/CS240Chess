package ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.AuthData;
import model.GameData;
import chess.ChessMove;
import chess.ChessPosition;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

@ClientEndpoint
public class ChessClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private AuthData authData;
    private List<GameData> currentGameList;
    private GameData currentGame;
    private boolean isBlackPerspective;
    private Session webSocketSession;
    private final Gson gson = new Gson();

    private enum State { PRELOGIN, PASTLOGIN, GAMEPLAY }
    private State state;

    public ChessClient(int port) {
        this.serverFacade = new ServerFacade(port);
        this.scanner = new Scanner(System.in);
        this.authData = null;
        this.currentGameList = null;
        this.currentGame = null;
        this.isBlackPerspective = false;
        this.state = State.PRELOGIN;
    }

    public void run() {
        System.out.println("Welcome to Chess Client");
        while (true) {
            switch (state) {
                case PRELOGIN -> runPreLogin();
                case PASTLOGIN -> runPastLogin();
                case GAMEPLAY -> runGameplay();
            }
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.webSocketSession = session;
        System.out.println("Connected to server (WebSocket open).");
        // Only attempt CONNECT if we actually have a valid currentGame & authData
        if (authData != null && currentGame != null) {
            sendConnectCommand();
        }
    }

    @OnMessage
    public void onMessage(String message) {
        JsonObject json = gson.fromJson(message, JsonObject.class);
        String type = json.get("serverMessageType").getAsString();
        switch (type) {
            case "LOAD_GAME" -> {
                // The server is giving us an updated GameData
                GameData updatedGame = gson.fromJson(json.get("game"), GameData.class);
                updateGameState(updatedGame);
            }
            case "NOTIFICATION" -> {
                String notification = json.get("message").getAsString();
                displayNotification(notification);
            }
            case "ERROR" -> {
                String error = json.get("errorMessage").getAsString();
                displayError(error);
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.webSocketSession = null;
        System.out.println("Disconnected from server (WebSocket close): " + reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

    private void sendConnectCommand() {
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "CONNECT");
            cmd.addProperty("authToken", authData.authToken());
            cmd.addProperty("gameID", currentGame.gameID());
            // Send it
            webSocketSession.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException e) {
            displayError("Error sending CONNECT command: " + e.getMessage());
        }
    }

    private void runPreLogin() {
        System.out.print(">>> ");
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length == 0) return;
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help" -> showPreLoginHelp();
            case "quit" -> {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            case "login" -> login();
            case "register" -> register();
            default -> System.out.println("Unknown command. Type 'help' for commands.");
        }
    }

    private void showPreLoginHelp() {
        System.out.println("Available commands:");
        System.out.println("  help - Show this help message");
        System.out.println("  quit - Exit the program");
        System.out.println("  login - Log in with username and password");
        System.out.println("  register - Register a new user");
    }

    private void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        try {
            authData = serverFacade.login(username, password);
            System.out.println("Logged in as " + authData.username());
            state = State.PASTLOGIN;
        } catch (ClientException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private void register() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        try {
            authData = serverFacade.register(username, password, email);
            System.out.println("Registered and logged in as " + authData.username());
            state = State.PASTLOGIN;
        } catch (ClientException e) {
            System.out.println("Register failed: " + e.getMessage());
        }
    }
    private void runPastLogin() {
        System.out.print(">>> ");
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length == 0) return;
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help" -> pastLoginHelp();
            case "logout" -> logout();
            case "create" -> createGame();
            case "list" -> listGames();
            case "play" -> playGame();
            case "observe" -> observeGame();
            default -> System.out.println("Unknown command. Type 'help' for commands.");
        }
    }

    private void pastLoginHelp() {
        System.out.println("Available commands:");
        System.out.println("  help - Show this help message");
        System.out.println("  logout - Log out and return to pre-login");
        System.out.println("  create - Create a new game");
        System.out.println("  list - List all existing games");
        System.out.println("  play - Join a game as a player");
        System.out.println("  observe - Observe a game");
    }

    private void logout() {
        try {
            serverFacade.logout(authData.authToken());
            System.out.println("Logged out successfully.");
            authData = null;
            state = State.PRELOGIN;
        } catch (ClientException e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
    }

    private void createGame() {
        System.out.print("Enter game name: ");
        String gameName = scanner.nextLine().trim();
        if (gameName.isEmpty()) {
            System.out.println("Game name cannot be empty.");
            return;
        }
        try {
            serverFacade.createGame(authData.authToken(), gameName);
            System.out.println("Game created");
        } catch (ClientException e) {
            System.out.println("Create game failed: " + e.getMessage());
        }
    }

    private void listGames() {
        try {
            currentGameList = serverFacade.listGames(authData.authToken());
            if (currentGameList.isEmpty()) {
                System.out.println("No games available.");
            } else {
                System.out.println("List of games:");
                for (int i = 0; i < currentGameList.size(); i++) {
                    GameData game = currentGameList.get(i);
                    String white = (game.whiteUsername() != null) ? game.whiteUsername() : "None";
                    String black = (game.blackUsername() != null) ? game.blackUsername() : "None";
                    System.out.printf("%d. Name: %s - White: %s, Black: %s%n",
                            i + 1, game.gameName(), white, black);
                }
            }
        } catch (ClientException e) {
            System.out.println("List games failed: " + e.getMessage());
        }
    }

    private void playGame() {
        GameData game = selectGameByNumber();
        if (game == null) return;

        System.out.print("Enter color (white/black): ");
        String color = scanner.nextLine().trim().toLowerCase();
        if (!color.equals("white") && !color.equals("black")) {
            System.out.println("Invalid color. Must be 'white' or 'black'.");
            return;
        }
        try {
            GameData updatedGame = serverFacade.joinGame(authData.authToken(), color, game.gameID());
            currentGame = updatedGame;
            isBlackPerspective = color.equals("black");
            System.out.println("Joined game " + updatedGame.gameName() + " as " + color);
            PrintBoard.drawChessBoard(updatedGame.game().getBoard(), isBlackPerspective);
            connectWebSocket();
            state = State.GAMEPLAY;
        } catch (ClientException e) {
            System.out.println("Failed to join game: " + e.getMessage());
        }
    }

    private void observeGame() {
        GameData game = selectGameByNumber();
        if (game == null) return;
        try {
            currentGame = game;
            isBlackPerspective = false;
            System.out.println("Observing game: " + game.gameName());
            PrintBoard.drawChessBoard(game.game().getBoard(), isBlackPerspective);
            connectWebSocket(); // triggers @OnOpen -> sendConnectCommand
            state = State.GAMEPLAY;
        } catch (Exception e) {
            System.out.println("Failed to observe game: " + e.getMessage());
        }
    }

    private GameData selectGameByNumber() {
        if (currentGameList == null || currentGameList.isEmpty()) {
            System.out.println("No games available. Please run 'list' first.");
            return null;
        }
        System.out.println("Enter the game number from the list (e.g. '1' for the first game):");
        System.out.print("Game number: ");
        String numberStr = scanner.nextLine().trim();
        int number;
        try {
            number = Integer.parseInt(numberStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return null;
        }
        if (number < 1 || number > currentGameList.size()) {
            System.out.printf("Invalid game number. Must be between 1 and %d%n", currentGameList.size());
            return null;
        }
        return currentGameList.get(number - 1);
    }

    private void connectWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI("ws://localhost:8080/ws"));
        } catch (Exception e) {
            System.out.println("Failed to connect to WebSocket: " + e.getMessage());
        }
    }

    private void runGameplay() {
        System.out.print(">>> ");
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length == 0) return;
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help" -> showGameplayHelp();
            case "redraw" -> redrawChessBoard();
            case "leave" -> leaveGame();
            case "move" -> makeMove();
            case "resign" -> resignGame();
            case "highlight" -> highlightLegalMoves();
            default -> System.out.println("Unknown command. Type 'help' for commands.");
        }
    }

    private void showGameplayHelp() {
        System.out.println("Gameplay Commands:");
        System.out.println("  help - Show this help message");
        System.out.println("  redraw - Redraw the chess board");
        System.out.println("  leave - Leave the game and return to post-login");
        System.out.println("  move - Make a move (format: startRow startCol endRow endCol)");
        System.out.println("  resign - Resign from the game");
        System.out.println("  highlight - Highlight legal moves for a piece (format: row col)");
    }

    private void makeMove() {
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            System.out.println("WebSocket is not connected. Cannot make move.");
            return;
        }
        System.out.println("Enter your move: [startRow startCol endRow endCol]");
        System.out.print(">>> ");
        String line = scanner.nextLine().trim();
        String[] parts = line.split("\\s+");
        if (parts.length != 4) {
            System.out.println("Invalid input. Must be 4 integers (startRow startCol endRow endCol).");
            return;
        }
        try {
            int startRow = Integer.parseInt(parts[0]);
            int startCol = Integer.parseInt(parts[1]);
            int endRow   = Integer.parseInt(parts[2]);
            int endCol   = Integer.parseInt(parts[3]);

            // Construct JSON for the MAKE_MOVE command
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "MAKE_MOVE");
            cmd.addProperty("authToken", authData.authToken());
            cmd.addProperty("gameID", currentGame.gameID());

            // Nested "move": { "start": {"row": X, "col": Y}, "end": {"row": X, "col": Y} }
            JsonObject moveObj = new JsonObject();
            JsonObject startObj = new JsonObject();
            startObj.addProperty("row", startRow);
            startObj.addProperty("col", startCol);
            JsonObject endObj = new JsonObject();
            endObj.addProperty("row", endRow);
            endObj.addProperty("col", endCol);

            moveObj.add("start", startObj);
            moveObj.add("end", endObj);
            cmd.add("move", moveObj);

            webSocketSession.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Rows and columns must be integers.");
        } catch (IOException e) {
            System.out.println("Error sending MAKE_MOVE: " + e.getMessage());
        }
    }

    private void redrawChessBoard() {
        if (currentGame != null) {
            PrintBoard.drawChessBoard(currentGame.game().getBoard(), isBlackPerspective);
        } else {
            System.out.println("No active game to redraw.");
        }
    }

    private void leaveGame() {
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            System.out.println("Not currently connected to a game.");
            return;
        }
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "LEAVE");
            cmd.addProperty("authToken", authData.authToken());
            cmd.addProperty("gameID", currentGame.gameID());
            webSocketSession.getBasicRemote().sendText(gson.toJson(cmd));

            // Then close WebSocket
            webSocketSession.close();
            state = State.PASTLOGIN;
            System.out.println("Left the game.");
        } catch (IOException e) {
            System.out.println("Error leaving game: " + e.getMessage());
        }
    }

    private void resignGame() {
        System.out.println("Are you sure you want to resign? (yes/no)");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes")) {
            try {
                JsonObject cmd = new JsonObject();
                cmd.addProperty("commandType", "RESIGN");
                cmd.addProperty("authToken", authData.authToken());
                cmd.addProperty("gameID", currentGame.gameID());
                webSocketSession.getBasicRemote().sendText(gson.toJson(cmd));
                // Stay connected, but the game is over
                System.out.println("You have resigned from the game.");
                state = State.PASTLOGIN;
            } catch (IOException e) {
                System.out.println("Error resigning: " + e.getMessage());
            }
        }
    }

    private void highlightLegalMoves() {
        System.out.print("Enter piece position (row col): ");
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Invalid input. Use: row col");
            return;
        }
        try {
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);
            if (currentGame == null) {
                System.out.println("No active game to highlight moves.");
                return;
            }
            ChessPosition pos = new ChessPosition(row, col);
            Collection<ChessMove> moves = currentGame.game().validMoves(pos);
            if (moves == null || moves.isEmpty()) {
                System.out.println("No legal moves for this piece.");
            } else {
                PrintBoard.printChessBoardWithHighlights(currentGame.game().getBoard(), moves, isBlackPerspective);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid position: row and column must be numbers.");
        }
    }

    // HELPER METHODS
    private void updateGameState(GameData updatedGame) {
        this.currentGame = updatedGame;
        PrintBoard.drawChessBoard(updatedGame.game().getBoard(), isBlackPerspective);
    }

    private void displayNotification(String message) {
        System.out.println("Notification: " + message);
    }

    private void displayError(String message) {
        System.out.println("Error: " + message);
    }

    public static void main(String[] args) {
        ChessClient client = new ChessClient(8080);
        client.run();
    }
}