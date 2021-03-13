package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public interface MovesRepository extends JpaRepository<Move, Integer> {

    default Move createNewMove(Game game, String notation) {
        return save(new Move(game, notation));
    }

}