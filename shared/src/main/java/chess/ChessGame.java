package chess;

import java.util.ArrayList;
import java.util.Collection;


/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTeam;
    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.currentTeam = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece currPiece = board.getPiece(startPosition);
        if (currPiece == null) {
            return null;  //没有棋子直接返回null
        }
        // 获取原生的所有可能走法
        Collection<ChessMove> rawMoves = currPiece.pieceMoves(board, startPosition);
        // 如果没有走法，直接返回null
        if (rawMoves == null || rawMoves.isEmpty()) {
            return null;
        }
        ArrayList<ChessMove> possibleMoves = new ArrayList<>(rawMoves);
        ArrayList<ChessMove> validMoves = new ArrayList<>();
        // 逐一测试每个潜在走法，判断走完后自己方是否会被将军
        for (ChessMove move : possibleMoves) {
            // 记录目标位置原本的棋子，以便还原
            ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
            // 1. 把当前棋子从起始位置拿走
            board.addPiece(startPosition, null);
            // 2. 放到目标位置（可能吃掉对方棋子）
            board.addPiece(move.getEndPosition(), currPiece);
            // 如果走完不会让自己方被将军，则此走法有效
            if (!isInCheck(currPiece.getTeamColor())) {
                validMoves.add(move);
            }
            // 还原棋盘
            board.addPiece(move.getEndPosition(), capturedPiece);
            board.addPiece(startPosition, currPiece);
        }
        return validMoves;
    }


    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException();
    }


    public boolean isInCheck(TeamColor teamColor) {
        // 1. 找到指定颜色的王
        ChessPosition kingPosition = findKingPosition(board, teamColor);
        if (kingPosition == null) {
            // 理论上不该发生，如果王都不在，则当做被将军处理
            return true;
        }
        // 2. 遍历敌方所有棋子，看它们是否能走到(吃掉)王的位置
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece EnemyPosition = board.getPiece(new ChessPosition(row, col));
                if (EnemyPosition == null || EnemyPosition.getTeamColor() == teamColor) {
                    continue; // 检查是否为enemy
                }
                Collection<ChessMove> enemyMoves = EnemyPosition.pieceMoves(board, new ChessPosition(row, col));
                // 判断能否攻击到king
                if (enemyMoves != null) {
                    for (ChessMove move : enemyMoves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;  // 有一步能打到王
                        }
                    }
                }
            }
        }
        return false;
    }


    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)){
            return false;
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition StalematePosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(StalematePosition);
                //如果为空并且颜色不对
                if (piece != null && piece.getTeamColor() == teamColor) {
                    //如果没有可以走的路线
                    Collection<ChessMove> moves = validMoves(StalematePosition);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
            return false;
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition StalematePosition = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(StalematePosition);
                //如果为空并且颜色不对
                if (piece != null && piece.getTeamColor() == teamColor) {
                    //如果没有可以走的路线
                    Collection<ChessMove> moves = validMoves(StalematePosition);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private ChessPosition findKingPosition(ChessBoard Board,
                                           TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = Board.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }
    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}