package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class PrintBoard {
    private static final int BOARD_SIZE = 8;
    private static final String[] COL_LABELS = {"a", "b", "c", "d", "e", "f", "g", "h"};

    public static void drawChessBoard(ChessBoard board, boolean asBlack) {
        System.out.print(EscapeSequences.ERASE_SCREEN);
        System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);

        if (asBlack) {
            drawBlackPerspective(board);
        } else {
            drawWhitePerspective(board);
        }

        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }

    private static void drawWhitePerspective(ChessBoard board) {
        // Column labels at the top (a to h)
        printColumnLabels(false);

        // Draw rows from 8 to 1 (top to bottom)
        for (int row = BOARD_SIZE; row >= 1; row--) {
            // Row label on the left
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN);
            System.out.print(row + " ");
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);

            // Draw squares for this row
            for (int col = 1; col <= BOARD_SIZE; col++) {
                drawSquare(board, row, col, (row + col) % 2 == 0);
            }

            // Reset background color and draw right label
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN);
            System.out.print(" " + row);
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);

            // Clear the rest of the line to prevent black space on the right
            System.out.print(EscapeSequences.ERASE_LINE);
            System.out.println();
        }

        // Column labels at the bottom
        printColumnLabels(false);
    }

    private static void drawBlackPerspective(ChessBoard board) {
        // Column labels at the top (h to a)
        printColumnLabels(true);

        // Draw rows from 1 to 8 (top to bottom)
        for (int row = 1; row <= BOARD_SIZE; row++) {
            // Row label on the left
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN);
            System.out.print(row + " ");
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);

            // Draw squares for this row
            for (int col = BOARD_SIZE; col >= 1; col--) {
                drawSquare(board, row, col, (row + col) % 2 == 0);
            }

            // Reset background color and draw right label
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN);
            System.out.print(" " + row);
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);

            // Clear the rest of the line to prevent black space on the right
            System.out.print(EscapeSequences.ERASE_LINE);
            System.out.println();
        }

        // Column labels at the bottom
        printColumnLabels(true);
    }

    private static void printColumnLabels(boolean reverse) {
        System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        System.out.print("  "); // Align with row labels
        for (int i = 0; i < BOARD_SIZE; i++) {
            int idx = reverse ? (BOARD_SIZE - 1 - i) : i;
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN);
            System.out.print(" " + COL_LABELS[idx] + " ");
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        }
        // Clear the rest of the line after column labels
        System.out.print(EscapeSequences.ERASE_LINE);
        System.out.println();
    }

    private static void drawSquare(ChessBoard board, int row, int col, boolean isLightSquare) {
        String squareColor = isLightSquare ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;
        System.out.print(squareColor);

        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        String pieceSymbol = getPieceSymbol(piece);
        String pieceColor = (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE) ?
                EscapeSequences.SET_TEXT_COLOR_WHITE : EscapeSequences.SET_TEXT_COLOR_BLACK;

        System.out.print(pieceColor);
        System.out.print(pieceSymbol);
        System.out.print(squareColor);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        if (piece == null) return EscapeSequences.EMPTY;

        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        return switch (piece.getPieceType()) {
            case KING -> isWhite ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            case QUEEN -> isWhite ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            case BISHOP -> isWhite ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> isWhite ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            case ROOK -> isWhite ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            case PAWN -> isWhite ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
        };
    }
}