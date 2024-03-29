package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovesRepository extends JpaRepository<Move, Integer> {

    default Move createNewMove(Game game, Move lastMove, String notation, String fen) {
        return save(new Move(game, lastMove, notation, fen));
    }

    default Move updateMove(Move move, Move nextMove) {
        move.setNextMove(nextMove);
        return save(move);
    }

    <T> T findFirstByGame_IdOrderByIdDesc(int gameId, Class<T> view);

    <T> T findByIdEquals(int id, Class<T> view);

}
