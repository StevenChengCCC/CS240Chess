package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveCalculator implements PieceMovesCalculator {
    private void promote (Collection<ChessMove> possibleMoves,ChessPosition myPosition,ChessPosition moveonestep){

        possibleMoves.add(new ChessMove(myPosition, moveonestep, ChessPiece.PieceType.QUEEN));
        possibleMoves.add(new ChessMove(myPosition, moveonestep, ChessPiece.PieceType.ROOK));
        possibleMoves.add(new ChessMove(myPosition, moveonestep, ChessPiece.PieceType.BISHOP));
        possibleMoves.add(new ChessMove(myPosition, moveonestep, ChessPiece.PieceType.KNIGHT));
    }
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        // 获取当前兵
        ChessPiece Pawn = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = Pawn.getTeamColor();

        int PawnRow = myPosition.getRow();
        int PawnCol = myPosition.getColumn();

        //白兵向上，黑兵向下
        int dir;
        if (myColor == ChessGame.TeamColor.WHITE) {
            dir = 1;
        } else {
            dir = -1;
        }

        //前进一格
        int rowonestep = PawnRow + dir;
        ChessPosition moveonestep = new ChessPosition(rowonestep, PawnCol);

        if (border(moveonestep)) {
            ChessPiece MoveOneStep = board.getPiece(moveonestep);

            // 必须前方无棋子才能前进
            if (MoveOneStep == null) {

                // 检测升变
                // 如果是白兵并且走到第8行
                if (myColor == ChessGame.TeamColor.WHITE) {
                    if (rowonestep == 8) {
                        // 四种升变
                        promote(possibleMoves,myPosition,moveonestep);
                    } else {
                        // 未到达升变行
                        possibleMoves.add(new ChessMove(myPosition, moveonestep, null));

                        // 起始两步(白兵在第2行)
                        if (PawnRow == 2) {
                            int movetwostepRow = rowonestep + dir; // row+2
                            ChessPosition moveTwoStep = new ChessPosition(movetwostepRow, PawnCol);
                            if (border(moveTwoStep)) {
                                ChessPiece MoveTwoStep = board.getPiece(moveTwoStep);
                                if (MoveTwoStep == null) {
                                    possibleMoves.add(new ChessMove(myPosition, moveTwoStep, null));
                                }
                            }
                        }
                    }
                }

                // 如果是黑兵并且走到第1行
                if (myColor == ChessGame.TeamColor.BLACK) {
                    if (rowonestep == 1) {
                        promote(possibleMoves,myPosition,moveonestep);
                    } else {
                        // 未到达升变行
                        possibleMoves.add(new ChessMove(myPosition, moveonestep, null));

                        // 起始两步(黑兵在第7行)
                        if (PawnRow == 7) {
                            int movetwostepRow = rowonestep + dir; // row-2
                            ChessPosition moveTwoStep = new ChessPosition(movetwostepRow, PawnCol);
                            if (border(moveTwoStep)) {
                                ChessPiece occupantTwoStep = board.getPiece(moveTwoStep);
                                if (occupantTwoStep == null) {
                                    possibleMoves.add(new ChessMove(myPosition, moveTwoStep, null));
                                }
                            }
                        }
                    }
                }

            }
        }

        // 斜着吃子
        // 左前方
        int leftCol = PawnCol - 1;
        if (leftCol >= 1) {
            ChessPosition LeftEat = new ChessPosition(rowonestep, leftCol);
            if (border(LeftEat)) {
                ChessPiece KOBE = board.getPiece(LeftEat);
                if (KOBE != null && KOBE.getTeamColor() != myColor) {
                    if (myColor == ChessGame.TeamColor.WHITE && rowonestep == 8) {
                        promote(possibleMoves,myPosition,LeftEat);
                    } else {
                        possibleMoves.add(new ChessMove(myPosition, LeftEat, null));
                    }
                    if (myColor == ChessGame.TeamColor.BLACK &&(rowonestep == 1)) {
                        promote(possibleMoves,myPosition,LeftEat);
                    } else {
                        possibleMoves.add(new ChessMove(myPosition, LeftEat, null));
                    }
                }
            }
        }

        // 右前方
        int rightCol = PawnCol + 1;
        if (rightCol <= 8) { // 棋盘范围
            ChessPosition RightEat = new ChessPosition(rowonestep, rightCol);
            if (border(RightEat)) {
                ChessPiece KOBEE = board.getPiece(RightEat);
                if (KOBEE != null && KOBEE.getTeamColor() != myColor) {
                    // 升变
                    if (myColor == ChessGame.TeamColor.WHITE && rowonestep == 8) {
                        promote(possibleMoves,myPosition, RightEat);
                    } else {
                        possibleMoves.add(new ChessMove(myPosition, RightEat, null));
                    }
                    if (myColor == ChessGame.TeamColor.BLACK && rowonestep == 1) {
                        promote(possibleMoves,myPosition, RightEat);
                    } else {
                        possibleMoves.add(new ChessMove(myPosition, RightEat, null));
                    }
                }
            }
        }

        return possibleMoves;
    }
}
