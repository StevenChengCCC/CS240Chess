package chess;

import java.util.Collection;

public class KingMoveCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        // 国王可能走的8个方向
        int[] rowDir = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] colDir = {-1, -1, -1, 0, 0, 1, 1, 1};
        return calculateMoves(board, myPosition, rowDir, colDir);
    }
}
