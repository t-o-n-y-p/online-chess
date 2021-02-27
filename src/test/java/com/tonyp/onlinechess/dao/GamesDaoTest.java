package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.TestConfiguration;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GamesDaoTest {

    @Autowired
    private EntityManager manager;

    @Autowired
    private GamesDao gamesDao;

    private List<User> allCreatedUsers;
    private List<Game> allCreatedGames;

    @Before
    public void setUp() throws InterruptedException {
        allCreatedUsers = new ArrayList<>();
        allCreatedGames = new ArrayList<>();
        manager.getTransaction().begin();
        for (int i = 0; i < 12; i++) {
            for (String prefix : List.of("login", "test")) {
                User user = new User(prefix + i, "password" + i);
                allCreatedUsers.add(user);
                manager.persist(user);
            }
        }
        Color currentColor = Color.WHITE;
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
                    allCreatedGames.add(game);
                    manager.persist(game);
                }
                sleep(100);
            }
        }
        for (User player1 : allCreatedUsers.subList(0, 16)) {
            for (User player2 : allCreatedUsers.subList(0, 10)) {
                if (!player1.equals(player2)) {
                    Game game = new Game(player1, player2);
                    allCreatedGames.add(game);
                    manager.persist(game);
                    sleep(100);
                    game = new Game(player2, player1);
                    game.setPlayerToMove(player1);
                    allCreatedGames.add(game);
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
                    allCreatedGames.add(game);
                    manager.persist(game);
                }
                sleep(100);
            }
        }
        manager.getTransaction().commit();
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void findByUser() {
        User noGames = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("[^\\d]+[1][0-1]$"))
                .findAny()
                .get();
        assertTrue(gamesDao.findByUser(noGames, 0, 1000).isEmpty());

        for (String regex : List.of("[^\\d]+[0-4]$", "[^\\d]+[5-7]$", "[^\\d]+[8-9]$")) {
            User player = allCreatedUsers.stream()
                    .filter(u -> u.getLogin().matches(regex))
                    .findAny()
                    .get();
            List<Game> actualResult = gamesDao.findByUser(player, 0, 1000);
            List<Game> expectedResult = allCreatedGames.stream()
                    .filter(g -> g.getWhite().equals(player) || g.getBlack().equals(player))
                    .sorted(Comparator.comparing(Game::isCompleted)
                            .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                            .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                    .collect(Collectors.toList());
            assertEquals(expectedResult, actualResult);
        }

        User player = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("[^\\d]+[0-4]$"))
                .findAny()
                .get();
        List<Game> result1 = gamesDao.findByUser(player, 0, 10);
        assertEquals(10, result1.size());
        List<Game> result2 = gamesDao.findByUser(player, 40, 10);
        assertEquals(10, result2.size());
        result1.retainAll(result2);
        assertTrue(result1.isEmpty());

        assertTrue(gamesDao.findByUser(player, 1000, 10).isEmpty());

        result1 = gamesDao.findByUser(player, 0, 25);
        assertEquals(25, result1.size());
        result2 = gamesDao.findByUser(player, 75, 25);
        assertEquals(19, result2.size());
        result1.retainAll(result2);
        assertTrue(result1.isEmpty());

    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void findByUserAndOpponentLoginInput() {
        User noGames = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("[^\\d]+[1][0-1]$"))
                .findAny()
                .get();
        assertTrue(gamesDao.findByUserAndOpponentLoginInput(noGames, "login", 0, 1000).isEmpty());
        User playerNegative = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("[^\\d]+[0-4]$"))
                .findAny()
                .get();
        assertTrue(gamesDao.findByUserAndOpponentLoginInput(playerNegative, "login_test", 0, 1000).isEmpty());

        for (String regex : List.of("login[0-4]$", "login[5-7]$", "login[8-9]$", "test[0-4]$", "test[5-7]$", "test[8-9]$")) {
            for (String input : List.of("login", "test")) {
                User player = allCreatedUsers.stream()
                        .filter(u -> u.getLogin().matches(regex))
                        .findAny()
                        .get();
                List<Game> actualResult = gamesDao.findByUserAndOpponentLoginInput(player, input, 0, 1000);
                List<Game> expectedResult = allCreatedGames.stream()
                        .filter(g -> (g.getWhite().equals(player) && g.getBlack().getLogin().contains(input))
                                || (g.getBlack().equals(player) && g.getWhite().getLogin().contains(input)))
                        .sorted(Comparator.comparing(Game::isCompleted)
                                .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                                .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                        .collect(Collectors.toList());
                assertEquals(expectedResult, actualResult);
            }
        }

        User player = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("login[0-4]$"))
                .findAny()
                .get();
        List<Game> result1 = gamesDao.findByUserAndOpponentLoginInput(player, "test", 0, 5);
        assertEquals(5, result1.size());
        List<Game> result2 = gamesDao.findByUserAndOpponentLoginInput(player, "test", 20, 5);
        assertEquals(5, result2.size());
        result1.retainAll(result2);
        assertTrue(result1.isEmpty());

        assertTrue(gamesDao.findByUserAndOpponentLoginInput(player, "test", 1000, 10).isEmpty());

        result1 = gamesDao.findByUserAndOpponentLoginInput(player, "test", 0, 12);
        assertEquals(12, result1.size());
        result2 = gamesDao.findByUserAndOpponentLoginInput(player, "test", 48, 12);
        assertEquals(3, result2.size());
        result1.retainAll(result2);
        assertTrue(result1.isEmpty());
    }
}