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
        int[] RowDir = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] ColDir = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < RowDir.length; i++) {
            int newRow = queenRow + RowDir[i];
            int newCol = queenCol + ColDir[i];

            // 不断往该方向走
            while (true) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);

                if (!border(newPosition)) {
                    break;
                }
                ChessPiece NewP = board.getPiece(newPosition);
                if (NewP == null) {
                    // 空位
                    possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    // 检测颜色
                    if (NewP.getTeamColor() != myColor) {
                        possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break; // 不管是敌方还是我方都停止
                }
                // 往下一个格子
                newRow += RowDir[i];
                newCol += ColDir[i];
            }
        }
        return possibleMoves;
    }
}
