package chess;

import java.util.Collection;

public class BishopMoveCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        // 象的4个方向：四个对角线
        int[] rowDir = {-1, -1, 1, 1};
        int[] colDir = {-1, 1, -1, 1};
        return calculateAdvancedMoves(board, myPosition, rowDir, colDir);
    }
}
