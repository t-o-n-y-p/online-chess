package com.tonyp.onlinechess.tools;

import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class GameUtil {

    private GameUtil(){
    }

    public static final String STARTING_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public static final String STARTING_POSITION_LEGAL_MOVES
            = "a2a3 b2b3 c2c3 d2d3 e2e3 f2f3 g2g3 h2h3 a2a4 b2b4 c2c4 d2d4 e2e4 f2f4 g2g4 h2h4 b1a3 b1c3 g1f3 g1h3";

    public static final List<String> SQUARES = IntStream.rangeClosed('a', 'h')
            .mapToObj(file -> IntStream.rangeClosed('1', '8')
                    .mapToObj(rank -> new String(new char[]{(char) file, (char) rank})))
            .flatMap(e -> e)
            .collect(Collectors.toList());

    private static final Map<String, String> CHESS_PIECES = Map.ofEntries(
            Map.entry(" ", ""),
            Map.entry("K", "♔"),
            Map.entry("Q", "♕"),
            Map.entry("R", "♖"),
            Map.entry("B", "♗"),
            Map.entry("N", "♘"),
            Map.entry("P", "♙"),
            Map.entry("k", "♚"),
            Map.entry("q", "♛"),
            Map.entry("r", "♜"),
            Map.entry("b", "♝"),
            Map.entry("n", "♞"),
            Map.entry("p", "♟")
    );

    public static List<List<String>> getBoard(String fen, Color color) {
        List<List<String>> board = Arrays.stream(
                Pattern.compile("[1-8]")
                        .matcher(fen.replaceAll("[\\s].*", ""))
                        .replaceAll(mr -> " ".repeat(Integer.parseInt(mr.group())))
                        .split("/"))
                .map(r -> Arrays.stream(r.split("(?!^)"))
                        .map(CHESS_PIECES::get)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        if (color == Color.BLACK) {
            Collections.reverse(board);
            board.forEach(Collections::reverse);
        }
        return board;
    }

    public static String getPositionFromFen(String fen) {
        return Arrays.toString(Arrays.copyOfRange(fen.split("\\s"), 0, 4));
    }

    private static boolean isDrawByInsufficientMaterial(String fen) {
        return fen.matches("([1-8/]*[KkBbNn]){2,3}[1-8/]*[\\s].*");
    }

    private static boolean isDrawByFiftyMoveRule(String fen) {
        return Integer.parseInt(fen.split("\\s")[4]) >= 100;
    }

    private static boolean whiteToMove(String fen) {
        return fen.contains("w");
    }

    private static boolean isDrawByRepetition(String fen, List<Move> moveHistory) {
        if (moveHistory == null || moveHistory.isEmpty()) {
            return false;
        }
        List<String> positionHistory = moveHistory.stream()
                .map(Move::getRepetitionInfo)
                .collect(Collectors.toList());
        return Collections.frequency(positionHistory, getPositionFromFen(fen)) >= 2;
    }

    public static Result getResult(String fen, String legalMoves, List<Move> moveHistory)
            throws ExecutionException, InterruptedException {
        if (isDrawByInsufficientMaterial(fen)) {
            return Result.DRAW_BY_INSUFFICIENT_MATERIAL;
        } else if (isDrawByFiftyMoveRule(fen)) {
            return Result.DRAW_BY_FIFTY_MOVE_RULE;
        } else if (isDrawByRepetition(fen, moveHistory)) {
            return Result.DRAW_BY_REPETITION;
        } else if (!legalMoves.isBlank()) {
            return Result.UNDEFINED;
        } else if (StockfishUtil.getCheckers(fen).isBlank()) {
            return Result.DRAW_BY_STALEMATE;
        } else if (whiteToMove(fen)) {
            return Result.BLACK_WON_BY_CHECKMATE;
        }
        return Result.WHITE_WON_BY_CHECKMATE;
    }

    public static double getRatingDifference(User white, User black, Result result) {
        if (result == Result.UNDEFINED) {
            return 0.0;
        }
        double actualPointsForWhite = 0.0;
        if (result.getWinningSide() == Color.WHITE) {
            actualPointsForWhite = 1.0;
        } else if (result.getWinningSide() == null) {
            actualPointsForWhite = 0.5;
        }
        double expectedPointsForWhite = 1 / (1 + Math.pow(10.0, (black.getRating() - white.getRating()) / 400));
        return 20 * (actualPointsForWhite - expectedPointsForWhite);
    }

    public static boolean isIllegalMove(Game game, String notation) {
        return notation == null || notation.isBlank()
                || !Set.of(game.getLegalMoves().split("\\s")).contains(notation);
    }

}

