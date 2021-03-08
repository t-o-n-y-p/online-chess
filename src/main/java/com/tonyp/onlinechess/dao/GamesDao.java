package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Repository
public class GamesDao {

    private EntityManager manager;

    public GamesDao(@Autowired EntityManager manager) {
        this.manager = manager;
    }

    public Game createNewGame(User white, User black) {
        Game newGame = new Game(white, black);
        manager.persist(newGame);
        return newGame;
    }

    public Game updateGame(Game game, String fen, String legalMoves, boolean isCompleted, String description, Move move) {
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
        return manager.merge(game);
    }

    public Game updateGame(Game game, boolean isCompleted, String description) {
        game.setCompleted(isCompleted);
        game.setDescription(description);
        game.setLastModifiedTimestamp(Instant.now().atZone(ZoneId.of("GMT")).toLocalDateTime());
        return manager.merge(game);
    }

    public List<Game> findByUser(User user, int offset, int limit) {
        return manager.createQuery(
                "from Game where white = :user or black = :user " +
                        "order by case " +
                        "when isCompleted = true then 2" +
                        "when playerToMove = :user then 0 " +
                        "else 1 " +
                        "end, lastModifiedTimestamp desc", Game.class)
                .setParameter("user", user)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Game> findByUserAndOpponentLoginInput(User user, String input, int offset, int limit) {
        return manager.createQuery(
                "from Game g where (white = :user and g.black.login like concat('%', :input, '%')) " +
                        "or (black = :user and g.white.login like concat('%', :input, '%')) " +
                        "order by case " +
                        "when isCompleted = true then 2" +
                        "when playerToMove = :user then 0 " +
                        "else 1 " +
                        "end, lastModifiedTimestamp desc", Game.class)
                .setParameter("input", input)
                .setParameter("user", user)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

}

