package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        ChessPiece pawn = board.getPiece(myPosition);
        ChessGame.TeamColor pawnColor = pawn.getTeamColor();
        int pawnRow = myPosition.getRow();
        int pawnCol = myPosition.getColumn();
        int pawnDir;
        if (pawnColor == ChessGame.TeamColor.WHITE){
            pawnDir = 1;
        } else {
            pawnDir = -1;
        }
        // 往前一步
        processForwardMoves(possibleMoves, board, myPosition, pawnRow, pawnCol, pawnDir, pawnColor);

        // 左吃
        int leftCol = pawnCol - 1;
        if (leftCol >= 1) {
            ChessPosition newPawnPositionL = new ChessPosition(pawnRow + pawnDir, leftCol);
            if (border(newPawnPositionL)) {
                ChessPiece newPawnPosL = board.getPiece(newPawnPositionL);
                if ((newPawnPosL != null) && (newPawnPosL.getTeamColor() != pawnColor)) {
                    addPawnMove(possibleMoves, board, myPosition, newPawnPositionL, pawnColor);
                }
            }
        }

        // 右吃
        int rightCol = pawnCol + 1;
        if (rightCol <= 8) {
            ChessPosition newPawnPositionR = new ChessPosition(pawnRow + pawnDir, rightCol);
            if (border(newPawnPositionR)) {
                ChessPiece newPawnPosR = board.getPiece(newPawnPositionR);
                if (newPawnPosR != null && newPawnPosR.getTeamColor() != pawnColor) {
                    addPawnMove(possibleMoves, board, myPosition, newPawnPositionR, pawnColor);
                }
            }
        }
        return possibleMoves;
    }

    // Helper function to handle forward moves (one step and two steps)
    private void processForwardMoves(Collection<ChessMove> possibleMoves, ChessBoard board, ChessPosition myPosition,
                                     int pawnRow, int pawnCol, int pawnDir, ChessGame.TeamColor pawnColor) {
        // 往前一步
        int newPawnRow = pawnRow + pawnDir;
        ChessPosition newPawnPosition = new ChessPosition(newPawnRow, pawnCol);
        if (!border(newPawnPosition)) {
            return;
        }
        ChessPiece newPawnPos = board.getPiece(newPawnPosition);
        if (newPawnPos != null) {
            return;
        }
        // 使用接口中的addPawnMove
        addPawnMove(possibleMoves, board, myPosition, newPawnPosition, pawnColor);

        // Two-step move if pawn is in its initial position
        if ((pawnRow == 2 && pawnColor == ChessGame.TeamColor.WHITE) ||
                (pawnRow == 7 && pawnColor == ChessGame.TeamColor.BLACK)) {
            int newPawnRow2 = pawnRow + pawnDir * 2;
            ChessPosition newPawnPosition2 = new ChessPosition(newPawnRow2, pawnCol);
            if (!border(newPawnPosition2)) {
                return;
            }
            ChessPiece newPawnPos2 = board.getPiece(newPawnPosition2);
            if (newPawnPos2 == null) {
                possibleMoves.add(new ChessMove(myPosition, newPawnPosition2, null));
            }
        }
    }
}
