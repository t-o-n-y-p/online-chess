package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.TestConfiguration;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UsersDaoTest {

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    private List<User> allCreatedUsers;
    private User lowRatedUser;
    private User midRatedUser1;
    private User midRatedUser2;
    private User highRatedUser;

    @Before
    public void setUp() {
        allCreatedUsers = new ArrayList<>();
        double currentRating = 2800;
        manager.getTransaction().begin();
        for (int i = 0; i < 120; i++) {
            for (String prefix : List.of("login", "test")) {
                User user = new User(prefix + i, "password" + i);
                user.setRating(currentRating);
                currentRating -= 7.5;
                allCreatedUsers.add(user);
                manager.persist(user);
            }
        }
        lowRatedUser = new User("login120", "qwerty");
        lowRatedUser.setRating(0);
        manager.persist(lowRatedUser);
        midRatedUser1 = new User("test121", "qwerty");
        midRatedUser1.setRating(1680);
        manager.persist(midRatedUser1);
        midRatedUser2 = new User("login121", "qwerty");
        midRatedUser2.setRating(1920);
        manager.persist(midRatedUser2);
        highRatedUser = new User("test120", "qwerty");
        highRatedUser.setRating(5000);
        manager.persist(highRatedUser);
        allCreatedUsers.add(lowRatedUser);
        allCreatedUsers.add(midRatedUser1);
        allCreatedUsers.add(midRatedUser2);
        allCreatedUsers.add(highRatedUser);
        manager.getTransaction().commit();
    }

    @Test
    public void findByLogin() {
        assertNull(usersDao.findByLogin("non existing user"));

        User found = usersDao.findByLogin("login100");
        assertNotNull(found);
        assertEquals("login100", found.getLogin());
    }

    @Test
    public void findByLoginAndPassword() {
        assertNull(usersDao.findByLoginAndPassword("some user", "password23"));
        assertNull(usersDao.findByLoginAndPassword("login56", "pass56"));

        User found = usersDao.findByLoginAndPassword("login82", "password82");
        assertNotNull(found);
        assertEquals("login82", found.getLogin());
        assertEquals("password82", found.getPassword());
    }

    @Test
    public void findOpponentsByRatingAndLoginInput() {
        assertTrue(usersDao.findOpponentsByRatingAndLoginInput(
                highRatedUser, "login", 3000, 50, 0, 1000
        ).isEmpty());
        assertTrue(usersDao.findOpponentsByRatingAndLoginInput(
                lowRatedUser, "login", 800, 50, 0, 1000
        ).isEmpty());
        assertTrue(usersDao.findOpponentsByRatingAndLoginInput(
                midRatedUser2, "login_test", 1900, 50, 0, 1000
        ).isEmpty());

        for (String input : List.of("login", "test")) {
            List<User> actualResult = usersDao.findOpponentsByRatingAndLoginInput(
                    highRatedUser, input, 3000, 400, 0, 1000
            );
            List<User> expectedResult = allCreatedUsers.stream()
                    .filter(u -> u.getRating() >= 2600 && u.getRating() <= 3400 && u.getLogin().contains(input))
                    .sorted(Comparator.comparing(User::getRating).reversed())
                    .collect(Collectors.toList());
            assertEquals(expectedResult, actualResult);
            actualResult = usersDao.findOpponentsByRatingAndLoginInput(
                    lowRatedUser, input, 800, 400, 0, 1000
            );
            expectedResult = allCreatedUsers.stream()
                    .filter(u -> u.getRating() >= 400 && u.getRating() <= 1200 && u.getLogin().contains(input))
                    .sorted(Comparator.comparing(User::getRating).reversed())
                    .collect(Collectors.toList());
            assertEquals(expectedResult, actualResult);
            actualResult = usersDao.findOpponentsByRatingAndLoginInput(
                    midRatedUser2, input, 1900, 50, 0, 1000
            );
            expectedResult = allCreatedUsers.stream()
                    .filter(u -> u.getRating() >= 1850 && u.getRating() <= 1950
                            && u.getLogin().contains(input) && !u.equals(midRatedUser2))
                    .sorted(Comparator.comparing(User::getRating).reversed())
                    .collect(Collectors.toList());
            assertEquals(expectedResult, actualResult);
            actualResult = usersDao.findOpponentsByRatingAndLoginInput(
                    midRatedUser1, input, 1900, 400, 0, 1000
            );
            expectedResult = allCreatedUsers.stream()
                    .filter(u -> u.getRating() >= 1500 && u.getRating() <= 2300
                            && u.getLogin().contains(input) && !u.equals(midRatedUser1))
                    .sorted(Comparator.comparing(User::getRating).reversed())
                    .collect(Collectors.toList());
            assertEquals(expectedResult, actualResult);
        }

        List<User> result1 = usersDao.findOpponentsByRatingAndLoginInput(
                midRatedUser1, "login", 1900, 400, 0, 10
        );
        assertEquals(10, result1.size());
        List<User> result2 = usersDao.findOpponentsByRatingAndLoginInput(
                midRatedUser1, "login", 1900, 400, 40, 10
        );
        assertEquals(10, result2.size());
        result1.retainAll(result2);
        assertTrue(result1.isEmpty());

        assertTrue(usersDao.findOpponentsByRatingAndLoginInput(
                midRatedUser1, "login", 1900, 400, 1000, 10
        ).isEmpty());

        result1 = usersDao.findOpponentsByRatingAndLoginInput(
                midRatedUser2, "login", 1900, 400, 0, 20
        );
        assertEquals(20, result1.size());
        result2 = usersDao.findOpponentsByRatingAndLoginInput(
                midRatedUser2, "login", 1900, 400, 40, 20
        );
        assertEquals(13, result2.size());
        result1.retainAll(result2);
        assertTrue(result1.isEmpty());

    }
}