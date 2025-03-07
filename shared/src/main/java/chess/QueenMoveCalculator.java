package chess;

import java.util.Collection;

public class QueenMoveCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        // 皇后8个方向：横、竖、斜
        int[] rowDir = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colDir = {-1, 0, 1, -1, 1, -1, 0, 1};
        return calculateAdvancedMoves(board, myPosition, rowDir, colDir);
    }
}
