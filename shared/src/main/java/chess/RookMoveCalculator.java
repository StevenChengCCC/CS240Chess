package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RookMoveCalculator implements PieceMovesCalculator{

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        // 从棋盘获得当前子的信息(阵营)
        ChessPiece Rook = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = Rook.getTeamColor();
        int RookRow = myPosition.getRow();
        int RookCol = myPosition.getColumn();
        //可能走的8个位置
        int[] NewRow = {1, -1, 0, 0, 2, -2, 0, 0 ,3 , -3,0,0,4,-4,0,0,5,-5,0,0,6,-6,0,0,7,-7,0,0,8,-8,0,0};
        int[] NewCol = {0, 0, 1, -1, 0, 0, 2, -2, 0,0,3,-3,0,0,4,-4,0,0,5,-5,0,0,6,-6,0,0,7,-7,0,0,8,-8};
        //row和col长度一样
        for (int i = 0; i < NewRow.length; i++) {
            int newRow = RookRow + NewRow[i];
            int newCol = RookCol + NewCol[i];
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
                    }else{
                        break;
                    }
                }
            }
        }
        return possibleMoves;
    }
}
