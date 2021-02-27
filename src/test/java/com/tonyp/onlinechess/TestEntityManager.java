package com.tonyp.onlinechess;

import com.tonyp.onlinechess.model.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static java.lang.Thread.sleep;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TestEntityManager {

    @Autowired
    private EntityManager manager;

    @Test
    public void smokeTest() throws InterruptedException {
        manager.getTransaction().begin();
        User player1 = new User("test" + System.currentTimeMillis(), "aaa");
        manager.persist(player1);
        manager.getTransaction().commit();
        sleep(10);
        manager.getTransaction().begin();
        User player2 = new User("test" + System.currentTimeMillis(), "aaa");
        manager.persist(player2);
        manager.getTransaction().commit();
        manager.getTransaction().begin();
        Game game = new Game(player1, player2);
        manager.persist(game);
        manager.getTransaction().commit();
        manager.getTransaction().begin();
        Challenge challenge = new Challenge(player1, player2, Color.WHITE);
        manager.persist(challenge);
        manager.getTransaction().commit();
        manager.getTransaction().begin();
        Move move = new Move(game, "e2e4");
        manager.persist(move);
        manager.getTransaction().commit();

        User foundUser = manager.find(User.class, player1.getId());
        Assert.assertNotNull(foundUser);
        foundUser = manager.find(User.class, player2.getId());
        Assert.assertNotNull(foundUser);
        Game foundGame = manager.find(Game.class, game.getId());
        Assert.assertNotNull(foundGame);
        Challenge foundChallenge = manager.find(Challenge.class, challenge.getId());
        Assert.assertNotNull(foundChallenge);
        Move foundMove = manager.find(Move.class, move.getId());
        Assert.assertNotNull(foundMove);
    }

}
