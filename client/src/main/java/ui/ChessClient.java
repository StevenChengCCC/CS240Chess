package ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.AuthData;
import model.GameData;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;

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
    private String userRole;
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
        this.userRole = null;
        this.state = State.PRELOGIN;
    }
    // websocket
    @OnOpen
    public void onOpen(Session session) {
        this.webSocketSession = session;
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
                ChessGame receivedGame = gson.fromJson(json.get("game"), ChessGame.class);
                if (currentGame == null) {
                    currentGame = new GameData(-1, null, null, null, receivedGame);
                } else {
                    currentGame = new GameData(
                            currentGame.gameID(),
                            currentGame.whiteUsername(),
                            currentGame.blackUsername(),
                            currentGame.gameName(),
                            receivedGame
                    );
                }
                updateGameState(currentGame);
            }
            case "NOTIFICATION" -> {
                String notice = json.get("message").getAsString();
                System.out.println(">> [SERVER NOTICE] " + notice);
            }
            case "ERROR" -> {
                String errorMessage = json.get("errorMessage").getAsString();
                System.out.println(">> [SERVER ERROR] " + errorMessage);
            }
            default -> {
                System.out.println(">> [SERVER] Unknown message: " + message);
            }
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.webSocketSession = null;
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket Error: " + throwable.getMessage());
    }

    private void sendConnectCommand() {
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "CONNECT");
            cmd.addProperty("authToken", authData.authToken());
            cmd.addProperty("gameID", currentGame.gameID());
            webSocketSession.getBasicRemote().sendText(gson.toJson(cmd));
        } catch (IOException e) {
            System.out.println("Failed to send CONNECT command: " + e.getMessage());
        }
    }

    //main function
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

    private void runPreLogin() {
        System.out.print(">>> ");
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length == 0) {
            return;
        }
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
        if (parts.length == 0) {
            return;
        }
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
                makeMove();
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
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Error: incorrect password")) {
                System.out.println("Login failed: wrong password");
            } else if (errorMessage.contains("Error: user does not exist")) {
                System.out.println("Login failed: user not exist, you need to register");
            } else if (errorMessage.contains("Error: missing or invalid request data")) {
                System.out.println("Login failed: please provide both username and password");
            } else {
                System.out.println("Login failed: " + errorMessage);
            }
            state = State.PRELOGIN;
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
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Error: username already taken")) {
                System.out.println("Register failed: username already taken");
            } else if (errorMessage.contains("Error: missing or invalid request data")) {
                System.out.println("Register failed: please provide username, password, and email");
            } else {
                System.out.println("Register failed: " + errorMessage);
            }
            state = State.PRELOGIN;
        }
    }

    private void logout() {
        try {
            serverFacade.logout(authData.authToken());
            System.out.println("Logged out successfully.");
            authData = null;
            state = State.PRELOGIN;
        } catch (ClientException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Error: invalid or missing authentication token")) {
                System.out.println("Logout failed: invalid session, please log in again");
                authData = null;
                state = State.PRELOGIN;
            } else {
                System.out.println("Logout failed: " + errorMessage);
            }
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
            int gameID = serverFacade.createGame(authData.authToken(), gameName);
            System.out.println("Created game created");
        } catch (ClientException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Error: invalid or missing authentication token")) {
                System.out.println("Create game failed: please log in again");
            } else if (errorMessage.contains("Error: missing or invalid request data")) {
                System.out.println("Create game failed: please provide a valid game name");
            } else {
                System.out.println("Create game failed: " + errorMessage);
            }
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
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Error: invalid or missing authentication token")) {
                System.out.println("List games failed: please log in again");
            } else {
                System.out.println("List games failed: " + errorMessage);
            }
        }
    }

    // play game
    private void playGame() {
        GameData chosen = selectGameByNumber();
        if (chosen == null) return;
        if (isGameOver(chosen)) {
            System.out.println("That game is already finished. Please pick another.");
            return;
        }

        System.out.print("Enter color (white/black): ");
        String color = scanner.nextLine().trim().toLowerCase();
        if (!color.equals("white") && !color.equals("black")) {
            System.out.println("Invalid color. Must be 'white' or 'black'.");
            return;
        }

        try {
            GameData updatedGame = serverFacade.joinGame(authData.authToken(), color, chosen.gameID());
            currentGame = updatedGame;
            userRole = color;
            isBlackPerspective = color.equals("black");
            System.out.println("Joined game " + updatedGame.gameName() + " as " + color);

            if (updatedGame.game() != null) {
                PrintBoard.drawChessBoard(updatedGame.game().getBoard(), isBlackPerspective);
            }
            connectWebSocket();
            state = State.GAMEPLAY;
        } catch (ClientException e) {
            System.out.println("Failed to join game: " + e.getMessage());
        }
    }

    private void observeGame() {
        GameData chosen = selectGameByNumber();
        if (chosen == null) return;

        if (isGameOver(chosen)) {
            System.out.println("That game is already finished. Please pick another.");
            return;
        }

        try {
            currentGame = chosen;
            userRole = "observer";
            isBlackPerspective = false;
            System.out.println("Observing game: " + chosen.gameName());

            if (chosen.game() != null) {
                PrintBoard.drawChessBoard(chosen.game().getBoard(), isBlackPerspective);
            }
            connectWebSocket();
            state = State.GAMEPLAY;
        } catch (Exception e) {
            System.out.println("Failed to observe: " + e.getMessage());
        }
    }

    private GameData selectGameByNumber() {
        if (currentGameList == null || currentGameList.isEmpty()) {
            System.out.println("No active games to choose from. Try 'list' first.");
            return null;
        }
        System.out.print("Choose a game number: ");
        String line = scanner.nextLine().trim();
        int choice;
        try {
            choice = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice.");
            return null;
        }
        if (choice < 1 || choice > currentGameList.size()) {
            System.out.println("Invalid game number.");
            return null;
        }
        return currentGameList.get(choice - 1);
    }

    private void connectWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI("ws://localhost:8080/ws"));
        } catch (Exception e) {
            System.out.println("WebSocket connect failed: " + e.getMessage());
        }
    }

    private void makeMove() {
        if (currentGame == null || currentGame.game() == null) {
            System.out.println("No active game or missing game data. Cannot move.");
            return;
        }
        if (userRole == null || userRole.equals("observer")) {
            System.out.println("You're only observing. You cannot make moves.");
            return;
        }
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            System.out.println("Not connected to server or WebSocket is closed.");
            return;
        }
        TeamColor currentTurn = currentGame.game().getTeamTurn();
        TeamColor myColor = userRole.equals("white") ? TeamColor.WHITE : TeamColor.BLACK;
        if (currentTurn != myColor) {
            System.out.println("It is not your turn. Please wait.");
            return;
        }

        System.out.println("Enter your move (startRow startCol endRow endCol): ");
        System.out.print(">>> ");
        String line = scanner.nextLine().trim();
        String[] parts = line.split("\\s+");
        if (parts.length != 4) {
            System.out.println("Invalid format. Must be 4 integers.");
            return;
        }

        try {
            int startRow = Integer.parseInt(parts[0]);
            int startCol = Integer.parseInt(parts[1]);
            int endRow   = Integer.parseInt(parts[2]);
            int endCol   = Integer.parseInt(parts[3]);

            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "MAKE_MOVE");
            cmd.addProperty("authToken", authData.authToken());
            cmd.addProperty("gameID", currentGame.gameID());

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
            System.out.println("Invalid input: must be integers.");
        } catch (IOException e) {
            System.out.println("Error sending move command: " + e.getMessage());
        }
    }

    private void highlightLegalMoves() {
        if (currentGame == null || currentGame.game() == null) {
            System.out.println("No active game or missing game data. Cannot highlight.");
            return;
        }
        System.out.print("Enter piece position (row col): ");
        String line = scanner.nextLine().trim();
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Invalid input. Must be 'row col'.");
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
            System.out.println("Positions must be integers.");
        }
    }

    private void resignGame() {
        if (currentGame == null || currentGame.game() == null) {
            System.out.println("No active game to resign from.");
            return;
        }
        if (userRole == null || userRole.equals("observer")) {
            System.out.println("Observers cannot resign.");
            return;
        }
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            System.out.println("Not connected. Cannot resign.");
            return;
        }

        System.out.println("Are you sure you want to resign? (yes/no)");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes")) {
            System.out.println("Resignation canceled.");
            return;
        }

        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "RESIGN");
            cmd.addProperty("authToken", authData.authToken());
            cmd.addProperty("gameID", currentGame.gameID());
            webSocketSession.getBasicRemote().sendText(gson.toJson(cmd));
            System.out.println("You resigned from the game.");
            state = State.PASTLOGIN;
        } catch (IOException e) {
            System.out.println("Error sending RESIGN command: " + e.getMessage());
        }
    }

    private void leaveGame() {
        if (currentGame == null || currentGame.game() == null) {
            System.out.println("No active game to leave.");
            return;
        }
        if (webSocketSession == null || !webSocketSession.isOpen()) {
            System.out.println("Already disconnected from this game.");
            state = State.PASTLOGIN;
            return;
        }
        try {
            JsonObject cmd = new JsonObject();
            cmd.addProperty("commandType", "LEAVE");
            cmd.addProperty("authToken", authData.authToken());
            cmd.addProperty("gameID", currentGame.gameID());
            webSocketSession.getBasicRemote().sendText(gson.toJson(cmd));

            webSocketSession.close();
            System.out.println("You left the game.");
            state = State.PASTLOGIN;
        } catch (IOException e) {
            System.out.println("Error leaving game: " + e.getMessage());
        }
    }

    private void redrawChessBoard() {
        if (currentGame != null && currentGame.game() != null) {
            PrintBoard.drawChessBoard(currentGame.game().getBoard(), isBlackPerspective);
        } else {
            System.out.println("No active game to redraw.");
        }
    }

    private void updateGameState(GameData updatedGame) {
        this.currentGame = updatedGame;
        if (updatedGame.game() != null) {
            PrintBoard.drawChessBoard(updatedGame.game().getBoard(), isBlackPerspective);
        } else {
            System.out.println("No board data to display.");
        }
    }
    private boolean isGameOver(GameData g) {
        if (g == null || g.game() == null) {
            return true;
        }
        ChessGame game = g.game();
        return game.isInCheckmate(TeamColor.WHITE)
                || game.isInCheckmate(TeamColor.BLACK)
                || game.isInStalemate(TeamColor.WHITE)
                || game.isInStalemate(TeamColor.BLACK);
    }
    public static void main(String[] args) {
        ChessClient client = new ChessClient(8080);
        client.run();
    }
}
