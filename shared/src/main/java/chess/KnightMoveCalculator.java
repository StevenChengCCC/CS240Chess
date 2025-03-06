package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMoveCalculator implements PieceMovesCalculator{

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        // 从棋盘获得当前子的信息(阵营)
        ChessPiece knight = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = knight.getTeamColor();
        int knightRow = myPosition.getRow();
        int knightCol = myPosition.getColumn();
        //可能走的8个位置
        int[] rowDir = {-1, 1, -2, 2, -2, 2, -1, 1};
        int[] colDir = {-2, -2, -1, -1, 1, 1, 2, 2};
        //row和col长度一样
        for (int i = 0; i < rowDir.length; i++) {
            int newRow = knightRow + rowDir[i];
            int newCol = knightCol + colDir[i];
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            // 判断棋盘边界
            if (border(newPosition)) {
                ChessPiece newP = board.getPiece(newPosition);
                if (newP == null) {
                    // 判断空位
                    possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    // 判断颜色
                    if (newP.getTeamColor() != myColor) {
                        possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }
        return possibleMoves;
    }
}
