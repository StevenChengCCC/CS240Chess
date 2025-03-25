package ui;

import model.AuthData;

import java.util.Scanner;

public class ChessClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private AuthData authData; // Tracks logged-in state

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
                handleLogin();
                break;
            case "register":
                handleRegister();
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

    private void handleLogin() {
        System.out.print("Username: ");

    }

    private void handleRegister() {
        System.out.print("Username: ");
    }

    public static void main(String[] args) {
        ChessClient client = new ChessClient();
        client.run();
    }
}