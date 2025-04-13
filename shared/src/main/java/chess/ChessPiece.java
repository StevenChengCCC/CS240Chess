package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }
    public PieceType getPieceType() {
        return type;
    }
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        switch (this.type) {
            case KING:
                possibleMoves.addAll(getKingMoves(board, myPosition));
                break;

            case QUEEN:
                possibleMoves.addAll(getQueenMoves(board, myPosition));
                break;

            case BISHOP:
                possibleMoves.addAll(getBishopMoves(board, myPosition));
                break;

            case KNIGHT:
                possibleMoves.addAll(getKnightMoves(board, myPosition));
                break;

            case ROOK:
                possibleMoves.addAll(getRookMoves(board, myPosition));
                break;

            case PAWN:
                possibleMoves.addAll(getPawnMoves(board, myPosition));
                break;
        }
        return possibleMoves;
    }
    private Collection<ChessMove> getKingMoves(ChessBoard board, ChessPosition myPosition) {
        return new KingMoveCalculator().pieceMoves(board, myPosition);
    }

    private Collection<ChessMove> getQueenMoves(ChessBoard board, ChessPosition myPosition) {
        return new QueenMoveCalculator().pieceMoves(board, myPosition);
    }

    private Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition myPosition) {
        return new BishopMoveCalculator().pieceMoves(board, myPosition);
    }

    private Collection<ChessMove> getKnightMoves(ChessBoard board, ChessPosition myPosition) {
        return new KnightMoveCalculator().pieceMoves(board, myPosition);
    }

    private Collection<ChessMove> getRookMoves(ChessBoard board, ChessPosition myPosition) {
        return new RookMoveCalculator().pieceMoves(board, myPosition);
    }

    private Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition myPosition) {
        return new PawnMoveCalculator().pieceMoves(board, myPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }
}
