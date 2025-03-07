package chess;

import java.util.Collection;

public class RookMoveCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        // 车的4个方向：上下左右
        int[] rowDir = {-1, 1, 0, 0};
        int[] colDir = {0, 0, -1, 1};
        return calculateAdvancedMoves(board, myPosition, rowDir, colDir);
    }
}
