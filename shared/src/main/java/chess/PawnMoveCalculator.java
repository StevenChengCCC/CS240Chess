package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        // 获取当前兵
        ChessPiece Pawn = board.getPiece(myPosition);
        ChessGame.TeamColor myColor = Pawn.getTeamColor();

        int pawnRow = myPosition.getRow();
        int pawnCol = myPosition.getColumn();
        int dir;
        if (myColor == ChessGame.TeamColor.WHITE) {
            dir = 1;
        } else {
            dir = -1;
        }

        //前进一格
        int rowOneStep = pawnRow + dir;
        ChessPosition moveOneStep = new ChessPosition(rowOneStep, pawnCol);

        if (border(moveOneStep)) {
            ChessPiece occupantOneStep = board.getPiece(moveOneStep);

            // 必须前方无棋子才能前进
            if (occupantOneStep == null) {

                // 检测升变
                // 如果是白兵并且走到第8行
                if (myColor == ChessGame.TeamColor.WHITE) {
                    if (rowOneStep == 8) {
                        // 四种升变
                        possibleMoves.add(new ChessMove(myPosition, moveOneStep, ChessPiece.PieceType.QUEEN));
                        possibleMoves.add(new ChessMove(myPosition, moveOneStep, ChessPiece.PieceType.ROOK));
                        possibleMoves.add(new ChessMove(myPosition, moveOneStep, ChessPiece.PieceType.BISHOP));
                        possibleMoves.add(new ChessMove(myPosition, moveOneStep, ChessPiece.PieceType.KNIGHT));
                    } else {
                        // 未到达升变行
                        possibleMoves.add(new ChessMove(myPosition, moveOneStep, null));

                        // 起始两步(白兵在第2行)
                        if (pawnRow == 2) {
                            int movetwostepRow = rowOneStep + dir; // row+2
                            ChessPosition moveTwoStep = new ChessPosition(movetwostepRow, pawnCol);
                            if (border(moveTwoStep)) {
                                ChessPiece occupantTwoStep = board.getPiece(moveTwoStep);
                                if (occupantTwoStep == null) {
                                    possibleMoves.add(new ChessMove(myPosition, moveTwoStep, null));
                                }
                            }
                        }
                    }
                }

                // 如果是黑兵并且走到第1行
                if (myColor == ChessGame.TeamColor.BLACK) {
                    if (rowOneStep == 1) {
                        // 四种升变
                        possibleMoves.add(new ChessMove(myPosition, moveOneStep, ChessPiece.PieceType.QUEEN));
                        possibleMoves.add(new ChessMove(myPosition, moveOneStep, ChessPiece.PieceType.ROOK));
                        possibleMoves.add(new ChessMove(myPosition, moveOneStep, ChessPiece.PieceType.BISHOP));
                        possibleMoves.add(new ChessMove(myPosition, moveOneStep, ChessPiece.PieceType.KNIGHT));
                    } else {
                        // 未到达升变行
                        possibleMoves.add(new ChessMove(myPosition, moveOneStep, null));

                        // 起始两步(黑兵在第7行)
                        if (pawnRow == 7) {
                            int movetwostepRow = rowOneStep + dir; // row-2
                            ChessPosition moveTwoStep = new ChessPosition(movetwostepRow, pawnCol);
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
        int leftCol = pawnCol - 1;
        if (leftCol >= 1) {
            ChessPosition leftCapturePos = new ChessPosition(rowOneStep, leftCol);
            if (border(leftCapturePos)) {
                ChessPiece occupant = board.getPiece(leftCapturePos);
                if (occupant != null && occupant.getTeamColor() != myColor) {
                    if (myColor == ChessGame.TeamColor.WHITE) {
                        if (rowOneStep == 8) {
                            possibleMoves.add(new ChessMove(myPosition, leftCapturePos, ChessPiece.PieceType.QUEEN));
                            possibleMoves.add(new ChessMove(myPosition, leftCapturePos, ChessPiece.PieceType.ROOK));
                            possibleMoves.add(new ChessMove(myPosition, leftCapturePos, ChessPiece.PieceType.BISHOP));
                            possibleMoves.add(new ChessMove(myPosition, leftCapturePos, ChessPiece.PieceType.KNIGHT));
                        } else {
                            possibleMoves.add(new ChessMove(myPosition, leftCapturePos, null));
                        }
                    }
                    if (myColor == ChessGame.TeamColor.BLACK) {
                        if (rowOneStep == 1) {
                            possibleMoves.add(new ChessMove(myPosition, leftCapturePos, ChessPiece.PieceType.QUEEN));
                            possibleMoves.add(new ChessMove(myPosition, leftCapturePos, ChessPiece.PieceType.ROOK));
                            possibleMoves.add(new ChessMove(myPosition, leftCapturePos, ChessPiece.PieceType.BISHOP));
                            possibleMoves.add(new ChessMove(myPosition, leftCapturePos, ChessPiece.PieceType.KNIGHT));
                        } else {
                            possibleMoves.add(new ChessMove(myPosition, leftCapturePos, null));
                        }
                    }
                }
            }
        }

        // 右前方
        int rightCol = pawnCol + 1;
        if (rightCol <= 8) { // 棋盘范围
            ChessPosition rightCapturePos = new ChessPosition(rowOneStep, rightCol);
            if (border(rightCapturePos)) {
                ChessPiece occupant = board.getPiece(rightCapturePos);
                if (occupant != null && occupant.getTeamColor() != myColor) {
                    // 升变
                    if (myColor == ChessGame.TeamColor.WHITE) {
                        if (rowOneStep == 8) {
                            possibleMoves.add(new ChessMove(myPosition, rightCapturePos, ChessPiece.PieceType.QUEEN));
                            possibleMoves.add(new ChessMove(myPosition, rightCapturePos, ChessPiece.PieceType.ROOK));
                            possibleMoves.add(new ChessMove(myPosition, rightCapturePos, ChessPiece.PieceType.BISHOP));
                            possibleMoves.add(new ChessMove(myPosition, rightCapturePos, ChessPiece.PieceType.KNIGHT));
                        } else {
                            possibleMoves.add(new ChessMove(myPosition, rightCapturePos, null));
                        }
                    }
                    if (myColor == ChessGame.TeamColor.BLACK) {
                        if (rowOneStep == 1) {
                            possibleMoves.add(new ChessMove(myPosition, rightCapturePos, ChessPiece.PieceType.QUEEN));
                            possibleMoves.add(new ChessMove(myPosition, rightCapturePos, ChessPiece.PieceType.ROOK));
                            possibleMoves.add(new ChessMove(myPosition, rightCapturePos, ChessPiece.PieceType.BISHOP));
                            possibleMoves.add(new ChessMove(myPosition, rightCapturePos, ChessPiece.PieceType.KNIGHT));
                        } else {
                            possibleMoves.add(new ChessMove(myPosition, rightCapturePos, null));
                        }
                    }
                }
            }
        }

        return possibleMoves;
    }
}