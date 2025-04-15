package ui;
import chess.ChessPiece;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
public class ChessClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private final WebSocketClient webSocketHandler;
    private AuthData authData;
    private List<GameData> currentGameList;
    private GameData currentGame;
    private boolean isBlackPerspective;
    private String userRole;
    private final Gson gson = new Gson();

    private enum State { PRELOGIN, PASTLOGIN, GAMEPLAY }
    private State state;

    public ChessClient(int port) {
        this.serverFacade = new ServerFacade(port);
        this.scanner = new Scanner(System.in);
        this.webSocketHandler = new WebSocketClient(this);
        this.authData = null;
        this.currentGameList = null;
        this.currentGame = null;
        this.isBlackPerspective = false;
        this.userRole = null;
        this.state = State.PRELOGIN;
    }

    // Callback methods for WebSocketHandler
    public void updateGame(ChessGame game) {
        if (currentGame != null) {
            currentGame = new GameData(
                    currentGame.gameID(),
                    currentGame.whiteUsername(),
                    currentGame.blackUsername(),
                    currentGame.gameName(),
                    game
            );
            updateGameState(currentGame);
        }
    }
    public void showNotification(String message) {
        System.out.println(message);
    }
    public void showError(String errorMessage) {
        System.out.println(errorMessage);
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
    private void runPreLogin() {
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length == 0) {
            return;
        }
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help" -> showPreLoginHelp();
            case "quit" -> {
                System.out.println("Goodbye!");
                System.exit(0);}
            case "login" -> login();
            case "register" -> register();
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }
    private void runPastLogin() {
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length == 0) {
            return;
        }
        String command = parts[0].toLowerCase();
        switch (command) {
            case "help" -> pastLoginHelp();
            case "logout" -> logout();
            case "create" -> createGame();
            case "list" -> listGames();
            case "play" -> playGame();
            case "observe" -> observeGame();
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }
    private void runGameplay() {
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length == 0) {
            return;
        }
        String command = parts[0].toLowerCase();
        switch (command) {
            case "help" -> showGameplayHelp();
            case "redraw" -> redrawChessBoard();
            case "leave" -> leaveGame();
            case "move" -> makeMove();
            case "resign" -> resignGame();
            case "highlight" -> highlightLegalMoves();
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
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
    private void playGame() {
        GameData chosen = selectGameByNumber();
        if (chosen == null) {return;}
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
            webSocketHandler.connect(authData.authToken(), currentGame.gameID());
            state = State.GAMEPLAY;
        } catch (ClientException e) {
            System.out.println("Failed to join game: " + e.getMessage());
        }
    }
    private void observeGame() {
        GameData chosen = selectGameByNumber();
        if (chosen == null) {return;}
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
            webSocketHandler.connect(authData.authToken(), currentGame.gameID());
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
    private void makeMove() {
        if (currentGame == null || currentGame.game() == null) {
            System.out.println("No active game or missing game data. Cannot move.");
            return;
        }
        if (userRole == null || userRole.equals("observer")) {
            System.out.println("You're only observing. You cannot make moves.");
            return;
        }
        TeamColor currentTurn = currentGame.game().getTeamTurn();
        TeamColor myColor = userRole.equals("white") ? TeamColor.WHITE : TeamColor.BLACK;
        if (currentTurn != myColor) {
            System.out.println("It is not your turn. Please wait.");
            return;
        }

        System.out.println("Enter your move in algebraic notation, e.g. 'a2 a4': ");
        String line = scanner.nextLine().trim();
        String[] tokens = line.split("\\s+");
        if (tokens.length != 2) {
            System.out.println("Usage: move <start> <end>, e.g. 'a2 a4'");
            return;
        }
        String startNotation = tokens[0];
        String endNotation = tokens[1];

        try {
            ChessPosition startPos = parseAlgebraic(startNotation);
            ChessPosition endPos = parseAlgebraic(endNotation);
            ChessPiece piece = currentGame.game().getBoard().getPiece(startPos);
            ChessPiece.PieceType promotionPiece = null;
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                if ((piece.getTeamColor() == TeamColor.WHITE && endPos.getRow() == 8) ||
                        (piece.getTeamColor() == TeamColor.BLACK && endPos.getRow() == 1)) {
                    promotionPiece = promptPromotionChoice();
                }
            }
            ChessMove move = new ChessMove(startPos, endPos, promotionPiece);
            webSocketHandler.sendMakeMoveCommand(move);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid notation: " + e.getMessage());
        }
    }
    private ChessPiece.PieceType promptPromotionChoice() {
        while (true) {
            System.out.println("Promote pawn to: (Q, R, B, or N)?");
            System.out.print(">>> ");
            String input = scanner.nextLine().trim().toUpperCase();
            switch (input) {
                case "Q":
                    return ChessPiece.PieceType.QUEEN;
                case "R":
                    return ChessPiece.PieceType.ROOK;
                case "B":
                    return ChessPiece.PieceType.BISHOP;
                case "N":
                    return ChessPiece.PieceType.KNIGHT;
                default:
                    System.out.println("Invalid choice. Please enter Q, R, B, or N.");
            }
        }
    }
    private void highlightLegalMoves() {
        if (currentGame == null || currentGame.game() == null) {
            System.out.println("No active game or missing game data. Cannot highlight.");
            return;
        }
        System.out.println("Enter a square to highlight moves from (e.g. 'a2'):");
        System.out.print(">>> ");
        String square = scanner.nextLine().trim();

        ChessPosition pos;
        try {
            pos = parseAlgebraic(square);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid notation: " + e.getMessage());
            return;
        }
        Collection<ChessMove> moves = currentGame.game().validMoves(pos);
        if (moves == null || moves.isEmpty()) {
            System.out.println("No legal moves for this piece (or square is empty).");
        } else {
            PrintBoard.printChessBoardWithHighlights(currentGame.game().getBoard(), moves, isBlackPerspective);
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
        System.out.println("Are you sure you want to resign? (yes/no)");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes")) {
            System.out.println("Resignation canceled.");
            return;
        }
        webSocketHandler.sendResignCommand();
    }
    private void leaveGame() {
        if (currentGame == null || currentGame.game() == null) {
            System.out.println("No active game to leave.");
            return;
        }
        webSocketHandler.disconnect();
        System.out.println("You left the game.");
        state = State.PASTLOGIN;
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
    private ChessPosition parseAlgebraic(String square) {
        if (square == null || square.length() < 2 || square.length() > 3) {
            throw new IllegalArgumentException("Invalid square notation: " + square);
        }
        char fileChar = Character.toLowerCase(square.charAt(0));
        if (fileChar < 'a' || fileChar > 'h') {
            throw new IllegalArgumentException("File must be a-h, got: " + fileChar);
        }
        int col = fileChar - 'a' + 1;
        String rankStr = square.substring(1);
        int row;
        try {
            row = Integer.parseInt(rankStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Rank must be 1..8, got: " + rankStr);
        }
        if (row < 1 || row > 8) {
            throw new IllegalArgumentException("Rank must be between 1 and 8, got: " + row);
        }
        return new ChessPosition(row, col);
    }
    public static void main(String[] args) {
        ChessClient client = new ChessClient(8080);
        client.run();
    }
}