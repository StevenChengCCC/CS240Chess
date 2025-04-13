package ui;

import model.AuthData;
import model.GameData;


import java.util.List;
import java.util.Scanner;

public class ChessClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private AuthData authData;
    private List<GameData> currentGameList;
    private enum State { PRELOGIN, PASTLOGIN, GAMEPLAY }
    private State state;

    public ChessClient(int port) {
        this.serverFacade = new ServerFacade(port);
        this.scanner = new Scanner(System.in);
        this.authData = null;
        this.currentGameList = null;
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
                //redrawChessBoard();
                break;
            case "leave":
                //leaveGame();
                break;
            case "move":
                //makeMove();
                break;
            case "resign":
                //resignGame();
                break;
            case "highlight":
                //highlightLegalMoves();
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

    private void playGame() {
        GameData game = selectGameByNumber(); // make the top part a helper function to pass the code quilaity test
        if (game == null) {
            return;
        }
        System.out.print("Enter color (white/black): ");
        String color = scanner.nextLine().trim().toLowerCase();
        if (!color.equals("white") && !color.equals("black")) {
            System.out.println("Invalid color. Must be 'white' or 'black'.");
            return;
        }

        try {
            GameData updatedGame = serverFacade.joinGame(authData.authToken(), color, game.gameID());
            System.out.println("Joined game " + updatedGame.gameName() + " as " + color);
            boolean asBlack = color.equals("black");
            PrintBoard.drawChessBoard(updatedGame.game().getBoard(), asBlack);
            state = State.GAMEPLAY;
        } catch (ClientException e) {
            System.out.println("Failed to join game: " + e.getMessage());
        }
    }

    private void observeGame() {
        GameData game = selectGameByNumber();
        if (game == null) {
            return;
        }
        try {
            System.out.println("Observing game: " + game.gameName());
            PrintBoard.drawChessBoard(game.game().getBoard(), false);
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

    // game play state
    

    public static void main(String[] args) {
        ChessClient client = new ChessClient(8080);
        client.run();
    }
}