package chess;

import java.util.Collection;

public class KnightMoveCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        // 马可能走的8个L型方向
        int[] rowDir = {-1, 1, -2, 2, -2, 2, -1, 1};
        int[] colDir = {-2, -2, -1, -1, 1, 1, 2, 2};
        return calculateMoves(board, myPosition, rowDir, colDir);
    }
}
