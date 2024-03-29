package com.tonyp.onlinechess;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.MovesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class EntityManagerTest {
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private ChallengesRepository challengesRepository;
    @Autowired
    private GamesRepository gamesRepository;
    @Autowired
    private MovesRepository movesRepository;

    @Test
    @Transactional(rollbackFor = Exception.class)
    public void smokeTest() throws InterruptedException {
        User player1 = new User("test" + System.currentTimeMillis(), "aaa");
        usersRepository.save(player1);
        TimeUnit.MILLISECONDS.sleep(10);
        User player2 = new User("test" + System.currentTimeMillis(), "aaa");
        usersRepository.save(player2);
        Game game = new Game(player1, player2);
        gamesRepository.save(game);
        Challenge challenge = new Challenge(player1, player2, Color.WHITE);
        challengesRepository.save(challenge);
        Move move = new Move(game, null, "e2e4", "qwerty");
        movesRepository.save(move);

        Optional<User> foundUser = usersRepository.findById(player1.getId());
        Assert.assertTrue(foundUser.isPresent());
        foundUser = usersRepository.findById(player2.getId());
        Assert.assertTrue(foundUser.isPresent());
        Optional<Game> foundGame = gamesRepository.findById(game.getId());
        Assert.assertTrue(foundGame.isPresent());
        Optional<Challenge> foundChallenge = challengesRepository.findById(challenge.getId());
        Assert.assertTrue(foundChallenge.isPresent());
        Optional<Move> foundMove = movesRepository.findById(move.getId());
        Assert.assertTrue(foundMove.isPresent());
    }

}
