package chess;

import java.util.ArrayList;
import java.util.Collection;

public class QueenMoveCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        // 获取当前Queen
        ChessPiece queen = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = queen.getTeamColor();

        int queenRow = myPosition.getRow();
        int queenCol = myPosition.getColumn();

        // 皇后8个方向
        int[] rowDir = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] colDir = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < rowDir.length; i++) {
            int newRow = queenRow + rowDir[i];
            int newCol = queenCol + colDir[i];

            // 不断往该方向走
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
