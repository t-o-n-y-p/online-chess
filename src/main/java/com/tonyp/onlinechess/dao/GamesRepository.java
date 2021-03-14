package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;

@Repository
public interface GamesRepository extends JpaRepository<Game, Integer> {

    default Game createNewGame(User white, User black) {
        return save(new Game(white, black));
    }

    default Game updateGame(Game game, String fen, String legalMoves, boolean isCompleted, String description, Move move) {
        game.setFen(fen);
        game.setLegalMoves(legalMoves);
        game.setCompleted(isCompleted);
        game.setDescription(description);
        if (game.getMoves() == null) {
            game.setMoves(new ArrayList<>());
        }
        game.getMoves().add(move);
        game.setPlayerToMove(game.getPlayerToMove().equals(game.getWhite()) ? game.getBlack() : game.getWhite());
        game.setLastModifiedTimestamp(Instant.now().atZone(ZoneId.of("GMT")).toLocalDateTime());
        return save(game);
    }

    default Game updateGame(Game game, boolean isCompleted, String description) {
        game.setCompleted(isCompleted);
        game.setDescription(description);
        game.setLastModifiedTimestamp(Instant.now().atZone(ZoneId.of("GMT")).toLocalDateTime());
        return save(game);
    }

    @Query("from Game g where g.white = :user or g.black = :user order by case " +
            "when g.isCompleted = true then 2 " +
            "when g.playerToMove = :user then 0 " +
            "else 1 " +
            "end, g.lastModifiedTimestamp desc")
    Page<Game> findByUser(@Param("user") User user, Pageable pageable);

    @Query("from Game g where (g.white = :user and g.black.login like concat('%', :input, '%')) " +
            "or (g.black = :user and g.white.login like concat('%', :input, '%')) " +
            "order by case " +
            "when g.isCompleted = true then 2 " +
            "when g.playerToMove = :user then 0 " +
            "else 1 " +
            "end, g.lastModifiedTimestamp desc")
    Page<Game> findByUserAndOpponentLoginInput(@Param("user") User user, @Param("input") String input, Pageable pageable);

}

