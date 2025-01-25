package chess;

import java.util.Collection;

public interface PieceMovesCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
    default boolean border(ChessPosition legalPosition){
        int row=legalPosition.getRow();
        int col=legalPosition.getColumn();
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return false;
        } else {
            return true;
        }
    }
}