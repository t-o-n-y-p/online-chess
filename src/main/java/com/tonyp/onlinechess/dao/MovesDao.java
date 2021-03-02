package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class MovesDao {

    private EntityManager manager;

    public MovesDao(@Autowired EntityManager manager) {
        this.manager = manager;
    }

    public Move createNewMove(Game game, String notation) {
        Move newMove = new Move(game, notation);
        manager.persist(newMove);
        return newMove;
    }

}
