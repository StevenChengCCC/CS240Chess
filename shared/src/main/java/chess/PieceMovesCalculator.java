package chess;

import java.util.Collection;
import java.util.ArrayList;

public interface PieceMovesCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);

    default boolean border(ChessPosition legalPosition) {
        int row = legalPosition.getRow();
        int col = legalPosition.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    default void tryAddMove(Collection<ChessMove> possibleMoves, ChessBoard board, ChessPosition from, ChessPosition to, ChessGame.TeamColor myColor) {
        if (border(to)) {
            ChessPiece newP = board.getPiece(to);
            if (newP == null || newP.getTeamColor() != myColor) {//检查颜色
                possibleMoves.add(new ChessMove(from, to, null));
            }
        }
    }

    default Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, int[] rowDir, int[] colDir) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = piece.getTeamColor();
        for (int i = 0; i < rowDir.length; i++) {
            int newRow = myPosition.getRow() + rowDir[i];
            int newCol = myPosition.getColumn() + colDir[i];
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            tryAddMove(possibleMoves, board, myPosition, newPosition, myColor);
        }
        return possibleMoves;
    }
    default Collection<ChessMove> calculateAdvancedMoves(ChessBoard board, ChessPosition myPosition, int[] rowDir, int[] colDir) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        ChessPiece piece = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = piece.getTeamColor();
        for (int i = 0; i < rowDir.length; i++) {
            int newRow = myPosition.getRow() + rowDir[i];
            int newCol = myPosition.getColumn() + colDir[i];
            // 沿着同一方向不断走
            while (true) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                if (!border(newPosition)) {
                    break;
                }
                ChessPiece newP = board.getPiece(newPosition);
                if (newP == null) {//检查空位
                    possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    if (newP.getTeamColor() != myColor) {//检查队伍
                        possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;
                }
                newRow += rowDir[i];
                newCol += colDir[i];
            }
        }
        return possibleMoves;
    }
}