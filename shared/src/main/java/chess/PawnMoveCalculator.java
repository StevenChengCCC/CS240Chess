package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove>PossibleMoves = new ArrayList<>();
        ChessPiece Pawn = board.getPiece(myPosition);
        ChessGame.TeamColor PawnColor = Pawn.getTeamColor();
        int pawnRow = myPosition.getRow();
        int pawnCol = myPosition.getColumn();
        int pawnDir;
        if (PawnColor == ChessGame.TeamColor.WHITE){
            pawnDir = 1;
        } else {
            pawnDir = -1;
        }
        // 往前一步
        int newPawnRow = pawnRow + pawnDir;
        ChessPosition newPawnPosition = new ChessPosition(newPawnRow, pawnCol);
        if(border(newPawnPosition)) {
            ChessPiece newPawnPos = board.getPiece(newPawnPosition);
            if (newPawnPos == null) {
                // 使用接口中的addPawnMove
                addPawnMove(PossibleMoves, board, myPosition, newPawnPosition, PawnColor);
                if ((pawnRow == 2 && PawnColor == ChessGame.TeamColor.WHITE) || (pawnRow == 7 && PawnColor == ChessGame.TeamColor.BLACK)) {
                    int newPawnRow2 = pawnRow + pawnDir * 2;
                    ChessPosition NewPawnPosition2 = new ChessPosition(newPawnRow2, pawnCol);
                    if (border(NewPawnPosition2)) {
                        ChessPiece NewPawnPos2 = board.getPiece(NewPawnPosition2);
                        if (NewPawnPos2 == null) {
                            PossibleMoves.add(new ChessMove(myPosition, NewPawnPosition2, null));
                        }
                    }
                }
            }
        }
        // 左吃
        int leftCol = pawnCol - 1;
        if (leftCol >= 1) {
            ChessPosition NewPawnPositionL = new ChessPosition(newPawnRow, leftCol);
            if (border(NewPawnPositionL)) {
                ChessPiece NewPawnPosL = board.getPiece(NewPawnPositionL);
                if ((NewPawnPosL != null) && (NewPawnPosL.getTeamColor() != PawnColor)) {
                    addPawnMove(PossibleMoves, board, myPosition, NewPawnPositionL, PawnColor);
                }
            }
        }
        // 右吃
        int rightCol = pawnCol + 1;
        if (rightCol <= 8) {
            ChessPosition NewPawnPositionR = new ChessPosition(newPawnRow, rightCol);
            if (border(NewPawnPositionR)) {
                ChessPiece NewPawnPosR = board.getPiece(NewPawnPositionR);
                if (NewPawnPosR != null && NewPawnPosR.getTeamColor() != PawnColor) {
                    addPawnMove(PossibleMoves, board, myPosition, NewPawnPositionR, PawnColor);
                }
            }
        }
        return PossibleMoves;
    }
}
