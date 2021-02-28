package com.tonyp.onlinechess;

import com.tonyp.onlinechess.dao.ChallengesDao;
import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CreateDatabaseTest {

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private GamesDao gamesDao;

    @Autowired
    private ChallengesDao challengesDao;

    private final List<User> allCreatedUsers = new ArrayList<>();

    @Before
    public void setUp() throws InterruptedException {
        double currentRating = 2800;
        manager.getTransaction().begin();
        for (int i = 0; i < 120; i++) {
            for (String prefix : List.of("login", "test")) {
                User user = new User(prefix + i, "password" + i);
                user.setRating(currentRating);
                currentRating -= 7.5;
                manager.persist(user);
                if (i < 12) {
                    allCreatedUsers.add(user);
                }
            }
        }
        User lowRatedUser = new User("login120", "qwerty");
        lowRatedUser.setRating(0);
        manager.persist(lowRatedUser);
        User midRatedUser1 = new User("test121", "qwerty");
        midRatedUser1.setRating(1680);
        manager.persist(midRatedUser1);
        User midRatedUser2 = new User("login121", "qwerty");
        midRatedUser2.setRating(1920);
        manager.persist(midRatedUser2);
        User highRatedUser = new User("test120", "qwerty");
        highRatedUser.setRating(5000);
        manager.persist(highRatedUser);

        Color currentColor = Color.WHITE;
        for (User from : allCreatedUsers) {
            for (User to : allCreatedUsers.subList(0, 20)) {
                if (!from.equals(to)) {
                    Challenge challenge = new Challenge(from, to, currentColor);
                    manager.persist(challenge);
                    if (currentColor == Color.WHITE) {
                        currentColor = Color.BLACK;
                    } else {
                        currentColor = Color.WHITE;
                    }
                }
                sleep(100);
            }
        }
        currentColor = Color.WHITE;
        for (User white : allCreatedUsers.subList(0, 10)) {
            for (User black : allCreatedUsers.subList(0, 10)) {
                if (!white.equals(black)) {
                    Game game = new Game(white, black);
                    if (currentColor == Color.WHITE) {
                        currentColor = Color.BLACK;
                    } else {
                        game.setPlayerToMove(black);
                        currentColor = Color.WHITE;
                    }
                    manager.persist(game);
                }
                sleep(100);
            }
        }
        for (User player1 : allCreatedUsers.subList(0, 16)) {
            for (User player2 : allCreatedUsers.subList(0, 10)) {
                if (!player1.equals(player2)) {
                    Game game = new Game(player1, player2);
                    manager.persist(game);
                    sleep(100);
                    game = new Game(player2, player1);
                    game.setPlayerToMove(player1);
                    manager.persist(game);
                }
                sleep(100);
            }
        }
        for (User player1 : allCreatedUsers.subList(0, 20)) {
            for (User player2 : allCreatedUsers.subList(0, 10)) {
                if (!player1.equals(player2)) {
                    Game game = new Game(player1, player2);
                    game.setCompleted(true);
                    if (currentColor == Color.WHITE) {
                        currentColor = Color.BLACK;
                    } else {
                        game.setPlayerToMove(player2);
                        currentColor = Color.WHITE;
                    }
                    manager.persist(game);
                }
                sleep(100);
            }
        }
        manager.getTransaction().commit();
    }

    @Test
    public void dummy() {
        System.out.println(1);
    }

}
