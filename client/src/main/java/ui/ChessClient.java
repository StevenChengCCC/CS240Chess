package ui;

import model.AuthData;

import java.util.Scanner;

public class ChessClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private AuthData authData;

    public ChessClient() {
        this.serverFacade = new ServerFacade();
        this.scanner = new Scanner(System.in);
        this.authData = null;
    }

    public void run() {
        System.out.println("Welcome to Chess Client");

        while (true) {
            if (authData == null) {
                runPreLogin();
            } else {
                runPostlogin();
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

    private void runPostlogin() {
        System.out.print(">>> ");
        String input = scanner.nextLine().trim();
        String[] parts = input.split("\\s+");
        if (parts.length == 0) {
            return;
        }
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help":
                postLoginHelp();
                break;
            case "logout":
                logout();
                break;
            case "create":
                //createGame();
                break;
            case "list":
                //listGames();
                break;
            case "play":
                //playGame();
                break;
            case "observe":
                //observeGame();
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

    private void postLoginHelp() {
        System.out.println("Available commands:");
        System.out.println("  help - Show this help message");
        System.out.println("  logout - Log out and return to pre-login");
        System.out.println("  create - Create a new game");
        System.out.println("  list - List all existing games");
        System.out.println("  play - Join a game as a player");
        System.out.println("  observe - Observe a game");
    }

    private void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            authData = serverFacade.login(username, password);
            System.out.println("Logged in as " + authData.username());
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
        } catch (ClientException e) {
            System.out.println("Register failed: " + e.getMessage());
        }
    }

    private void logout() {
        try {
            serverFacade.logout(authData.authToken());
            System.out.println("Logged out successfully.");
            authData = null;
        } catch (ClientException e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ChessClient client = new ChessClient();
        client.run();
    }
}