package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessMove;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PrintBoard {
    private static final int BOARD_SIZE = 8;
    private static final String[] COL_LABELS = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private static final String HIGHLIGHT_BG = "\u001B[43m"; // Yellow background for highlights

    public static void drawChessBoard(ChessBoard board, boolean asBlack) {
        System.out.print(EscapeSequences.ERASE_SCREEN);
        System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);

        if (asBlack) {
            drawBlackPerspective(board, new HashSet<>());
        } else {
            drawWhitePerspective(board, new HashSet<>());
        }

        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }

    public static void printChessBoardWithHighlights(ChessBoard board, Collection<ChessMove> highlights, boolean asBlack) {
        Set<ChessPosition> highlightPositions = new HashSet<>();
        for (ChessMove move : highlights) {
            highlightPositions.add(move.getEndPosition());
            highlightPositions.add(move.getStartPosition());
        }
        System.out.print(EscapeSequences.ERASE_SCREEN);
        System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        System.out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);

        if (asBlack) {
            drawBlackPerspective(board, highlightPositions);
        } else {
            drawWhitePerspective(board, highlightPositions);
        }

        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.println();
    }

    private static void drawWhitePerspective(ChessBoard board, Set<ChessPosition> highlightPositions) {
        printColumnLabels(false);
        for (int row = BOARD_SIZE; row >= 1; row--) {
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN);
            System.out.print(row + " ");
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);

            for (int col = 1; col <= BOARD_SIZE; col++) {
                ChessPosition pos = new ChessPosition(row, col);
                drawSquare(board, pos, (row + col) % 2 == 0, highlightPositions.contains(pos));
            }

            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN);
            System.out.print(" " + row);
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            System.out.print(EscapeSequences.ERASE_LINE);
            System.out.println();
        }
        printColumnLabels(false);
    }

    private static void drawBlackPerspective(ChessBoard board, Set<ChessPosition> highlightPositions) {
        printColumnLabels(true);
        for (int row = 1; row <= BOARD_SIZE; row++) {
            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN);
            System.out.print(row + " ");
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);

            for (int col = BOARD_SIZE; col >= 1; col--) {
                ChessPosition pos = new ChessPosition(row, col);
                drawSquare(board, pos, (row + col) % 2 == 0, highlightPositions.contains(pos));
            }

            System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN);
            System.out.print(" " + row);
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            System.out.print(EscapeSequences.ERASE_LINE);
            System.out.println();
        }
        printColumnLabels(true);
    }

    private static void printColumnLabels(boolean reverse) {
        System.out.print(EscapeSequences.SET_BG_COLOR_BLACK);
        System.out.print("  ");
        for (int i = 0; i < BOARD_SIZE; i++) {
            int idx = reverse ? (BOARD_SIZE - 1 - i) : i;
            System.out.print(EscapeSequences.SET_TEXT_COLOR_GREEN);
            System.out.print(" " + COL_LABELS[idx] + " ");
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        }
        System.out.print(EscapeSequences.ERASE_LINE);
        System.out.println();
    }

    private static void drawSquare(ChessBoard board, ChessPosition pos, boolean isLightSquare, boolean isHighlighted) {
        String squareColor = isHighlighted ? HIGHLIGHT_BG :
                isLightSquare ? EscapeSequences.SET_BG_COLOR_DARK_GREY : EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        System.out.print(squareColor);

        ChessPiece piece = board.getPiece(pos);
        String pieceSymbol = getPieceSymbol(piece);
        String pieceColor = (piece != null && piece.getTeamColor() == ChessGame.TeamColor.WHITE) ?
                EscapeSequences.SET_TEXT_COLOR_WHITE : EscapeSequences.SET_TEXT_COLOR_BLACK;

        System.out.print(pieceColor);
        System.out.print(pieceSymbol);
        System.out.print(squareColor);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        }
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