package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMoveCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        // 获取当前Rook
        ChessPiece rook = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = rook.getTeamColor();

        int rookRow = myPosition.getRow();
        int rookCol = myPosition.getColumn();

        // 车的4个方向
        int[] rowDir = {-1, 1, 0, 0};
        int[] colDir = {0, 0, -1, 1};

        for (int i = 0; i < rowDir.length; i++) {
            int newRow = rookRow + rowDir[i];
            int newCol = rookCol + colDir[i];

            // 沿着方向一直走
            while (true) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);

                if (!border(newPosition)) {
                    break;
                }
                ChessPiece newP = board.getPiece(newPosition);
                if (newP == null) {
                    // 空位
                    possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    // 检测颜色
                    if (newP.getTeamColor() != myColor) {
                        possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break; // 不管是敌方还是我方都停止
                }
                // 往下一个格子
                newRow += rowDir[i];
                newCol += colDir[i];
            }
        }
        return possibleMoves;
    }
}
