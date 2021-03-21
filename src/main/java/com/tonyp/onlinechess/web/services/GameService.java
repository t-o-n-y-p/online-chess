package com.tonyp.onlinechess.web.services;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.MovesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.GameUtil;
import com.tonyp.onlinechess.tools.Result;
import com.tonyp.onlinechess.tools.StockfishUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class GameService {

    private final UsersRepository usersRepository;
    private final GamesRepository gamesRepository;
    private final MovesRepository movesRepository;

    @Transactional(rollbackFor = Exception.class)
    public void resign(Game game, User user) {
        Result currentResult = user.equals(game.getWhite())
                ? Result.BLACK_WON_BY_RESIGNATION
                : Result.WHITE_WON_BY_RESIGNATION;

        gamesRepository.updateGame(game, true, currentResult.getDescription());
        double ratingDifference = GameUtil.getRatingDifference(game.getWhite(), game.getBlack(), currentResult);
        usersRepository.updateRating(game.getWhite(), ratingDifference);
        usersRepository.updateRating(game.getBlack(), -ratingDifference);
    }

    @Transactional(rollbackFor = Exception.class)
    public void makeMove(Game game, String notation) throws ExecutionException, InterruptedException {
        String newFen = StockfishUtil.makeMove(game.getFen(), notation);
        String newLegalMoves = StockfishUtil.getLegalMoves(newFen);
        boolean isCompleted = false;
        String description = null;
        List<Move> previousMoves = game.getMoves();
        Result currentResult = GameUtil.getResult(newFen, newLegalMoves, previousMoves);
        if (currentResult != Result.UNDEFINED) {
            isCompleted = true;
            description = currentResult.getDescription();
        }

        Move lastMove = null;
        if (previousMoves != null && !previousMoves.isEmpty()) {
            lastMove = previousMoves.stream().max(Comparator.comparing(Move::getId)).get();
        }
        Move newMove = movesRepository.createNewMove(game, lastMove, notation, newFen);
        gamesRepository.updateGame(game, newFen, newLegalMoves, isCompleted, description, newMove);
        if (lastMove != null) {
            movesRepository.updateMove(lastMove, newMove);
        }
        if (currentResult != Result.UNDEFINED) {
            double ratingDifference = GameUtil.getRatingDifference(game.getWhite(), game.getBlack(), currentResult);
            usersRepository.updateRating(game.getWhite(), ratingDifference);
            usersRepository.updateRating(game.getBlack(), -ratingDifference);
        }
    }

}
