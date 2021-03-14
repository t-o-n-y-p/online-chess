package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.TestConfiguration;
import com.tonyp.onlinechess.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
public class UsersRepositoryTest {

    @Autowired
    private UsersRepository usersRepository;

    private List<User> allCreatedUsers;
    private User lowRatedUser;
    private User midRatedUser1;
    private User midRatedUser2;
    private User highRatedUser;

    @Before
    @Transactional(rollbackFor = Exception.class)
    public void setUp() {
        allCreatedUsers = new ArrayList<>();
        double currentRating = 2800;
        for (int i = 0; i < 120; i++) {
            for (String prefix : List.of("login", "test")) {
                User user = new User(prefix + i, "password" + i);
                user.setRating(currentRating);
                currentRating -= 7.5;
                allCreatedUsers.add(user);
                usersRepository.save(user);
            }
        }
        lowRatedUser = new User("login120", "qwerty");
        lowRatedUser.setRating(0);
        usersRepository.save(lowRatedUser);
        midRatedUser1 = new User("test121", "qwerty");
        midRatedUser1.setRating(1680);
        usersRepository.save(midRatedUser1);
        midRatedUser2 = new User("login121", "qwerty");
        midRatedUser2.setRating(1920);
        usersRepository.save(midRatedUser2);
        highRatedUser = new User("test120", "qwerty");
        highRatedUser.setRating(5000);
        usersRepository.save(highRatedUser);
        allCreatedUsers.add(lowRatedUser);
        allCreatedUsers.add(midRatedUser1);
        allCreatedUsers.add(midRatedUser2);
        allCreatedUsers.add(highRatedUser);
    }

    @Test
    public void findOpponentsByRatingAndLoginInput() {
        assertTrue(usersRepository.findOpponentsByRatingAndLoginInput(
                highRatedUser, "login", 2950, 3050, PageRequest.of(0, 1000)
        ).isEmpty());
        assertTrue(usersRepository.findOpponentsByRatingAndLoginInput(
                lowRatedUser, "login", 750, 850, PageRequest.of(0, 1000)
        ).isEmpty());
        assertTrue(usersRepository.findOpponentsByRatingAndLoginInput(
                midRatedUser2, "login_test", 1850, 1950, PageRequest.of(0, 1000)
        ).isEmpty());

        Page<User> actualResult;
        List<User> expectedResult;
        for (String input : List.of("login", "test")) {
            actualResult = usersRepository.findOpponentsByRatingAndLoginInput(
                    highRatedUser, input, 2600, 3400, PageRequest.of(0, 1000)
            );
            expectedResult = allCreatedUsers.stream()
                    .filter(u -> u.getRating() >= 2600 && u.getRating() <= 3400 && u.getLogin().contains(input))
                    .sorted(Comparator.comparing(User::getRating).reversed())
                    .collect(Collectors.toList());
            assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
            actualResult = usersRepository.findOpponentsByRatingAndLoginInput(
                    lowRatedUser, input, 400, 1200, PageRequest.of(0, 1000)
            );
            expectedResult = allCreatedUsers.stream()
                    .filter(u -> u.getRating() >= 400 && u.getRating() <= 1200 && u.getLogin().contains(input))
                    .sorted(Comparator.comparing(User::getRating).reversed())
                    .collect(Collectors.toList());
            assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
            actualResult = usersRepository.findOpponentsByRatingAndLoginInput(
                    midRatedUser2, input, 1850, 1950, PageRequest.of(0, 1000)
            );
            expectedResult = allCreatedUsers.stream()
                    .filter(u -> u.getRating() >= 1850 && u.getRating() <= 1950
                            && u.getLogin().contains(input) && !u.equals(midRatedUser2))
                    .sorted(Comparator.comparing(User::getRating).reversed())
                    .collect(Collectors.toList());
            assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
            actualResult = usersRepository.findOpponentsByRatingAndLoginInput(
                    midRatedUser1, input, 1500, 2300, PageRequest.of(0, 1000)
            );
            expectedResult = allCreatedUsers.stream()
                    .filter(u -> u.getRating() >= 1500 && u.getRating() <= 2300
                            && u.getLogin().contains(input) && !u.equals(midRatedUser1))
                    .sorted(Comparator.comparing(User::getRating).reversed())
                    .collect(Collectors.toList());
            assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        }

        actualResult = usersRepository.findOpponentsByRatingAndLoginInput(
                midRatedUser1, "login", 1500, 2300, PageRequest.of(0, 10)
        );
        expectedResult = allCreatedUsers.stream()
                .filter(u -> u.getRating() >= 1500 && u.getRating() <= 2300
                        && u.getLogin().contains("login") && !u.equals(midRatedUser1))
                .sorted(Comparator.comparing(User::getRating).reversed())
                .limit(10)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        actualResult = usersRepository.findOpponentsByRatingAndLoginInput(
                midRatedUser1, "login", 1500, 2300, PageRequest.of(4, 10)
        );
        expectedResult = allCreatedUsers.stream()
                .filter(u -> u.getRating() >= 1500 && u.getRating() <= 2300
                        && u.getLogin().contains("login") && !u.equals(midRatedUser1))
                .sorted(Comparator.comparing(User::getRating).reversed())
                .skip(40)
                .limit(10)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));

        assertTrue(usersRepository.findOpponentsByRatingAndLoginInput(
                midRatedUser1, "login", 1500, 2300, PageRequest.of(100, 10)
        ).isEmpty());

        actualResult = usersRepository.findOpponentsByRatingAndLoginInput(
                midRatedUser2, "login", 1500, 2300, PageRequest.of(0, 20)
        );
        expectedResult = allCreatedUsers.stream()
                .filter(u -> u.getRating() >= 1500 && u.getRating() <= 2300
                        && u.getLogin().contains("login") && !u.equals(midRatedUser2))
                .sorted(Comparator.comparing(User::getRating).reversed())
                .limit(20)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        actualResult = usersRepository.findOpponentsByRatingAndLoginInput(
                midRatedUser2, "login", 1500, 2300, PageRequest.of(2, 20)
        );
        expectedResult = allCreatedUsers.stream()
                .filter(u -> u.getRating() >= 1500 && u.getRating() <= 2300
                        && u.getLogin().contains("login") && !u.equals(midRatedUser2))
                .sorted(Comparator.comparing(User::getRating).reversed())
                .skip(40)
                .limit(20)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
    }
}