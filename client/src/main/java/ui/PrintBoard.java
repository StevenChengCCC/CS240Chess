package ui;

import chess.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PrintBoard {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW_BG = "\u001B[43m";
    private static final String ANSI_WHITE_BG = "\u001B[47m";
    private static final String ANSI_BLACK_BG = "\u001B[40m";

    public static void printChessBoard(ChessBoard board, boolean fromBlackPerspective) {
        printChessBoardWithHighlights(board, new HashSet<>(), fromBlackPerspective);
    }

    public static void printChessBoardWithHighlights(ChessBoard board, Collection<ChessMove> highlights, boolean fromBlackPerspective) {
        Set<ChessPosition> highlightPositions = new HashSet<>();
        for (ChessMove move : highlights) {
            highlightPositions.add(move.getEndPosition());
        }

        int startRow = fromBlackPerspective ? 1 : 8;
        int endRow = fromBlackPerspective ? 9 : 0;
        int rowStep = fromBlackPerspective ? 1 : -1;
        int startCol = fromBlackPerspective ? 8 : 1;
        int endCol = fromBlackPerspective ? 0 : 9;
        int colStep = fromBlackPerspective ? -1 : 1;

        System.out.print("  ");
        for (int col = startCol; col != endCol; col += colStep) {
            System.out.print(" " + col + " ");
        }
        System.out.println();

        for (int row = startRow; row != endRow; row += rowStep) {
            System.out.print(row + " ");
            for (int col = startCol; col != endCol; col += colStep) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                boolean isHighlighted = highlightPositions.contains(pos);
                boolean isWhiteSquare = (row + col) % 2 == 0;

                String bgColor = isHighlighted ? ANSI_YELLOW_BG : (isWhiteSquare ? ANSI_WHITE_BG : ANSI_BLACK_BG);
                String pieceSymbol = piece == null ? " " : getPieceSymbol(piece);

                System.out.print(bgColor + " " + pieceSymbol + " " + ANSI_RESET);
            }
            System.out.println(" " + row);
        }

        System.out.print("  ");
        for (int col = startCol; col != endCol; col += colStep) {
            System.out.print(" " + col + " ");
        }
        System.out.println();
    }

    private static String getPieceSymbol(ChessPiece piece) {
        String color = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "W" : "B";
        return switch (piece.getPieceType()) {
            case KING -> color + "K";
            case QUEEN -> color + "Q";
            case ROOK -> color + "R";
            case BISHOP -> color + "B";
            case KNIGHT -> color + "N";
            case PAWN -> color + "P";
        };
    }
}