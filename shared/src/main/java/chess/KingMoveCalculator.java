package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KingMoveCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        // 从棋盘获得当前子的信息(阵营)
        ChessPiece king = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = king.getTeamColor();
        int kingRow = myPosition.getRow();
        int kingCol = myPosition.getColumn();
        //可能走的8个位置
        int[] NewRow = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] NewCol = {-1, -1, -1, 0, 0, 1, 1, 1};
        //row和col长度一样
        for (int i = 0; i < NewRow.length; i++) {
            int newRow = kingRow + NewRow[i];
            int newCol = kingCol + NewCol[i];
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            // 判断棋盘边界
            if (border(newPosition)) {
                ChessPiece NewP = board.getPiece(newPosition);
                if (NewP == null) {
                    // 判断空位
                    possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    // 判断颜色
                    if (NewP.getTeamColor() != myColor) {
                        possibleMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }
        return possibleMoves;
    }
}
