package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.MoveView;
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

    MoveView findByIdEquals(int id);

}
