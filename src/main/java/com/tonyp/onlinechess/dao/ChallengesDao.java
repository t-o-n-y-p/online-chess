package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class ChallengesDao {

    private final EntityManager manager;

    public ChallengesDao(@Autowired EntityManager manager) {
        this.manager = manager;
    }

    public Challenge createNewChallenge(User from, User to, Color targetColor) {
        Challenge newChallenge = new Challenge(from, to, targetColor);
        manager.persist(newChallenge);
        return newChallenge;
    }

    public List<Challenge> findIncomingChallenges(User user, int offset, int limit) {
        return manager.createQuery(
                "from Challenge where to = :user order by timestamp desc", Challenge.class)
                .setParameter("user", user)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Challenge> findIncomingChallengesByOpponentLoginInput(User user, String input, int offset, int limit) {
        return manager.createQuery(
                "from Challenge c where to = :user and c.from.login like concat('%', :input, '%') " +
                        "order by timestamp desc", Challenge.class)
                .setParameter("input", input)
                .setParameter("user", user)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

}

