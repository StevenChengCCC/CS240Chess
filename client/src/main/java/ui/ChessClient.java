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
                case PRELOGIN:
                    runPreLogin();
                    break;
                case PASTLOGIN:
                    runPastLogin();
                    break;
                case GAMEPLAY:
                    runGameplay();
                    break;
            }
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.webSocketSession = session;
        System.out.println("Connected to server");
        sendConnectCommand();
    }

    @OnMessage
    public void onMessage(String message) {
        JsonObject json = gson.fromJson(message, JsonObject.class);
        String type = json.get("serverMessageType").getAsString();
        switch (type) {
            case "LOAD_GAME":
                GameData game = gson.fromJson(json.get("game"), GameData.class);
                updateGameState(game);
                break;
            case "NOTIFICATION":
                String notification = json.get("message").getAsString();
                displayNotification(notification);
                break;
            case "ERROR":
                String error = json.get("errorMessage").getAsString();
                displayError(error);
                break;
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.webSocketSession = null;
        System.out.println("Disconnected from server: " + reason);
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
            webSocketSession.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException e) {
            displayError("Error sending connect command: " + e.getMessage());
        }
    }

    private void runPreLogin() {
        System.out.print(">>> ");
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length == 0) return;
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help":
                showPreLoginHelp();
                break;
            case "quit":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            case "login":
                login();
                break;
            case "register":
                register();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void runPastLogin() {
        System.out.print(">>> ");
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length == 0) return;
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help":
                pastLoginHelp();
                break;
            case "logout":
                logout();
                break;
            case "create":
                createGame();
                break;
            case "list":
                listGames();
                break;
            case "play":
                playGame();
                break;
            case "observe":
                observeGame();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void runGameplay() {
        System.out.print(">>> ");
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length == 0) return;
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help":
                showGameplayHelp();
                break;
            case "redraw":
                redrawChessBoard();
                break;
            case "leave":
                leaveGame();
                break;
            case "move":
                //makeMove();
                break;
            case "resign":
                resignGame();
                break;
            case "highlight":
                highlightLegalMoves();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void showPreLoginHelp() {
        System.out.println("Available commands:");
        System.out.println("  help - Show this help message");
        System.out.println("  quit - Exit the program");
        System.out.println("  login - Log in with username and password");
        System.out.println("  register - Register a new user");
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

    private void showGameplayHelp() {
        System.out.println("Gameplay Commands:");
        System.out.println("  help - Show this help message");
        System.out.println("  redraw - Redraw the chess board");
        System.out.println("  leave - Leave the game and return to post-login");
        System.out.println("  move - Make a move (format: startRow startCol endRow endCol)");
        System.out.println("  resign - Resign from the game");
        System.out.println("  highlight - Highlight legal moves for a piece (format: row col)");
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
                    String white = game.whiteUsername() != null ? game.whiteUsername() : "None";
                    String black = game.blackUsername() != null ? game.blackUsername() : "None";
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
            connectWebSocket();
            state = State.GAMEPLAY;
        } catch (Exception e) {
            System.out.println("Failed to observe game: " + e.getMessage());
        }
    }

    private GameData selectGameByNumber() {
        if (currentGameList == null || currentGameList.isEmpty()) {
            System.out.println("No games available. Please run 'list' to see available games.");
            return null;
        }
        System.out.println("Enter the game number from the list (e.g., '1' for the first game):");
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
            System.out.printf("Invalid game number. Please enter a number between 1 and %d.%n", currentGameList.size());
            return null;
        }
        return currentGameList.get(number - 1);
    }

    private void connectWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI("ws://localhost:8080/ws"));
        } catch (Exception e) {
            System.out.println("Failed to connect to WebSocket server: " + e.getMessage());
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
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "LEAVE");
            cmd.addProperty("authToken", authData.authToken());
            cmd.addProperty("gameID", currentGame.gameID());
            webSocketSession.getBasicRemote().sendText(gson.toJson(cmd));
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
                state = State.PASTLOGIN;
                System.out.println("You have resigned from the game.");
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

    private void updateGameState(GameData game) {
        this.currentGame = game;
        PrintBoard.drawChessBoard(game.game().getBoard(), isBlackPerspective);
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