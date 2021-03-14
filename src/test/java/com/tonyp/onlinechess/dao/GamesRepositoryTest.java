package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.TestConfiguration;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
public class GamesRepositoryTest {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private GamesRepository gamesRepository;

    private List<User> allCreatedUsers;
    private List<Game> allCreatedGames;

    @Before
    @Transactional(rollbackFor = Exception.class)
    public void setUp() throws InterruptedException {
        allCreatedUsers = new ArrayList<>();
        allCreatedGames = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            for (String prefix : List.of("login", "test")) {
                User user = new User(prefix + i, "password" + i);
                allCreatedUsers.add(user);
                usersRepository.save(user);
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
                    gamesRepository.save(game);
                }
                sleep(100);
            }
        }
        for (User player1 : allCreatedUsers.subList(0, 16)) {
            for (User player2 : allCreatedUsers.subList(0, 10)) {
                if (!player1.equals(player2)) {
                    Game game = new Game(player1, player2);
                    allCreatedGames.add(game);
                    gamesRepository.save(game);
                    sleep(100);
                    game = new Game(player2, player1);
                    game.setPlayerToMove(player1);
                    allCreatedGames.add(game);
                    gamesRepository.save(game);
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
                    gamesRepository.save(game);
                }
                sleep(100);
            }
        }
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void findByUser() {
        User noGames = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("[^\\d]+[1][0-1]$"))
                .findAny()
                .get();
        assertTrue(gamesRepository.findByUser(noGames, PageRequest.of(0, 1000)).isEmpty());

        Page<Game> actualResult;
        List<Game> expectedResult;
        for (String regex : List.of("[^\\d]+[0-4]$", "[^\\d]+[5-7]$", "[^\\d]+[8-9]$")) {
            User player = allCreatedUsers.stream()
                    .filter(u -> u.getLogin().matches(regex))
                    .findAny()
                    .get();
            actualResult = gamesRepository.findByUser(player, PageRequest.of(0, 1000));
            expectedResult = allCreatedGames.stream()
                    .filter(g -> g.getWhite().equals(player) || g.getBlack().equals(player))
                    .sorted(Comparator.comparing(Game::isCompleted)
                            .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                            .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                    .collect(Collectors.toList());
            assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        }

        User player = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("[^\\d]+[0-4]$"))
                .findAny()
                .get();
        actualResult = gamesRepository.findByUser(player, PageRequest.of(0, 10));
        expectedResult = allCreatedGames.stream()
                .filter(g -> g.getWhite().equals(player) || g.getBlack().equals(player))
                .sorted(Comparator.comparing(Game::isCompleted)
                        .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                        .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                .limit(10)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        actualResult = gamesRepository.findByUser(player, PageRequest.of(4, 10));
        expectedResult = allCreatedGames.stream()
                .filter(g -> g.getWhite().equals(player) || g.getBlack().equals(player))
                .sorted(Comparator.comparing(Game::isCompleted)
                        .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                        .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                .skip(40)
                .limit(10)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));

        assertTrue(gamesRepository.findByUser(player, PageRequest.of(100, 10)).isEmpty());

        actualResult = gamesRepository.findByUser(player, PageRequest.of(0, 25));
        expectedResult = allCreatedGames.stream()
                .filter(g -> g.getWhite().equals(player) || g.getBlack().equals(player))
                .sorted(Comparator.comparing(Game::isCompleted)
                        .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                        .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                .limit(25)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        actualResult = gamesRepository.findByUser(player, PageRequest.of(3, 25));
        expectedResult = allCreatedGames.stream()
                .filter(g -> g.getWhite().equals(player) || g.getBlack().equals(player))
                .sorted(Comparator.comparing(Game::isCompleted)
                        .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                        .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                .skip(75)
                .limit(25)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));

    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void findByUserAndOpponentLoginInput() {
        User noGames = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("[^\\d]+[1][0-1]$"))
                .findAny()
                .get();
        assertTrue(gamesRepository.findByUserAndOpponentLoginInput(noGames, "login", PageRequest.of(0, 1000)).isEmpty());
        User playerNegative = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("[^\\d]+[0-4]$"))
                .findAny()
                .get();
        assertTrue(gamesRepository.findByUserAndOpponentLoginInput(playerNegative, "login_test", PageRequest.of(0, 1000)).isEmpty());

        Page<Game> actualResult;
        List<Game> expectedResult;
        for (String regex : List.of("login[0-4]$", "login[5-7]$", "login[8-9]$", "test[0-4]$", "test[5-7]$", "test[8-9]$")) {
            for (String input : List.of("login", "test")) {
                User player = allCreatedUsers.stream()
                        .filter(u -> u.getLogin().matches(regex))
                        .findAny()
                        .get();
                actualResult = gamesRepository.findByUserAndOpponentLoginInput(player, input, PageRequest.of(0, 1000));
                expectedResult = allCreatedGames.stream()
                        .filter(g -> (g.getWhite().equals(player) && g.getBlack().getLogin().contains(input))
                                || (g.getBlack().equals(player) && g.getWhite().getLogin().contains(input)))
                        .sorted(Comparator.comparing(Game::isCompleted)
                                .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                                .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                        .collect(Collectors.toList());
                assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
            }
        }

        User player = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("login[0-4]$"))
                .findAny()
                .get();
        actualResult = gamesRepository.findByUserAndOpponentLoginInput(player, "test", PageRequest.of(0, 5));
        expectedResult = allCreatedGames.stream()
                .filter(g -> (g.getWhite().equals(player) && g.getBlack().getLogin().contains("test"))
                        || (g.getBlack().equals(player) && g.getWhite().getLogin().contains("test")))
                .sorted(Comparator.comparing(Game::isCompleted)
                        .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                        .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                .limit(5)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        actualResult = gamesRepository.findByUserAndOpponentLoginInput(player, "test", PageRequest.of(4, 5));
        expectedResult = allCreatedGames.stream()
                .filter(g -> (g.getWhite().equals(player) && g.getBlack().getLogin().contains("test"))
                        || (g.getBlack().equals(player) && g.getWhite().getLogin().contains("test")))
                .sorted(Comparator.comparing(Game::isCompleted)
                        .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                        .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                .skip(20)
                .limit(5)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));

        assertTrue(gamesRepository.findByUserAndOpponentLoginInput(player, "test", PageRequest.of(100, 10)).isEmpty());

        actualResult = gamesRepository.findByUserAndOpponentLoginInput(player, "test", PageRequest.of(0, 12));
        expectedResult = allCreatedGames.stream()
                .filter(g -> (g.getWhite().equals(player) && g.getBlack().getLogin().contains("test"))
                        || (g.getBlack().equals(player) && g.getWhite().getLogin().contains("test")))
                .sorted(Comparator.comparing(Game::isCompleted)
                        .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                        .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                .limit(12)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        actualResult = gamesRepository.findByUserAndOpponentLoginInput(player, "test", PageRequest.of(4, 12));
        expectedResult = allCreatedGames.stream()
                .filter(g -> (g.getWhite().equals(player) && g.getBlack().getLogin().contains("test"))
                        || (g.getBlack().equals(player) && g.getWhite().getLogin().contains("test")))
                .sorted(Comparator.comparing(Game::isCompleted)
                        .thenComparing(g -> !g.getPlayerToMove().equals(player) && !g.isCompleted())
                        .thenComparing(Comparator.comparing(Game::getLastModifiedTimestamp).reversed()))
                .skip(48)
                .limit(12)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
    }
}