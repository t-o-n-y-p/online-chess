package com.tonyp.onlinechess.dao;

import com.tonyp.onlinechess.TestConfiguration;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
public class ChallengesRepositoryTest {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private ChallengesRepository challengesRepository;

    private List<User> allCreatedUsers;
    private List<Challenge> allCreatedChallenges;

    @Before
    @Transactional(rollbackFor = Exception.class)
    public void setUp() throws InterruptedException {
        allCreatedUsers = new ArrayList<>();
        allCreatedChallenges = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            for (String prefix : List.of("login", "test")) {
                User user = new User(prefix + i, "password" + i);
                allCreatedUsers.add(user);
                usersRepository.save(user);
            }
        }
        Color currentColor = Color.WHITE;
        for (User from : allCreatedUsers) {
            for (User to : allCreatedUsers.subList(0, 20)) {
                if (!from.equals(to)) {
                    Challenge challenge = new Challenge(from, to, currentColor);
                    allCreatedChallenges.add(challenge);
                    challengesRepository.save(challenge);
                    if (currentColor == Color.WHITE) {
                        currentColor = Color.BLACK;
                    } else {
                        currentColor = Color.WHITE;
                    }
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }
        }
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void findIncomingChallenges() {
        User noChallenges = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("[^\\d]+[1][0-1]$"))
                .findAny()
                .get();
        assertEquals(0, challengesRepository
                .findByToOrderByTimestampDesc(noChallenges, PageRequest.of(0, 1000))
                .getNumberOfElements()
        );

        User to = allCreatedUsers.stream()
                .filter(u -> !u.getLogin().matches("[^\\d]+[1][0-1]$"))
                .findAny()
                .get();
        Page<Challenge> actualResult = challengesRepository.findByToOrderByTimestampDesc(to, PageRequest.of(0, 1000));
        List<Challenge> expectedResult = allCreatedChallenges.stream()
                .filter(c -> c.getTo().equals(to))
                .sorted(Comparator.comparing(Challenge::getTimestamp).reversed())
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));

        actualResult = challengesRepository.findByToOrderByTimestampDesc(to, PageRequest.of(0, 5));
        expectedResult = allCreatedChallenges.stream()
                .filter(c -> c.getTo().equals(to))
                .sorted(Comparator.comparing(Challenge::getTimestamp).reversed())
                .limit(5)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        actualResult = challengesRepository.findByToOrderByTimestampDesc(to, PageRequest.of(2, 5));
        expectedResult = allCreatedChallenges.stream()
                .filter(c -> c.getTo().equals(to))
                .sorted(Comparator.comparing(Challenge::getTimestamp).reversed())
                .skip(10)
                .limit(5)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));

        assertEquals(0, challengesRepository
                .findByToOrderByTimestampDesc(to, PageRequest.of(100, 10))
                .getNumberOfElements()
        );

        actualResult = challengesRepository.findByToOrderByTimestampDesc(to, PageRequest.of(0, 10));
        expectedResult = allCreatedChallenges.stream()
                .filter(c -> c.getTo().equals(to))
                .sorted(Comparator.comparing(Challenge::getTimestamp).reversed())
                .limit(10)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        actualResult = challengesRepository.findByToOrderByTimestampDesc(to, PageRequest.of(2, 10));
        expectedResult = allCreatedChallenges.stream()
                .filter(c -> c.getTo().equals(to))
                .sorted(Comparator.comparing(Challenge::getTimestamp).reversed())
                .skip(20)
                .limit(10)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void findIncomingChallengesByOpponentLoginInput() {
        User noChallenges = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("[^\\d]+[1][0-1]$"))
                .findAny()
                .get();
        assertEquals(0, challengesRepository
                .findIncomingChallengesByOpponentLoginInput(noChallenges, "login",  PageRequest.of(0, 1000))
                .getNumberOfElements()
        );
        User toNegative = allCreatedUsers.stream()
                .filter(u -> !u.getLogin().matches("[^\\d]+[1][0-1]$"))
                .findAny()
                .get();
        assertEquals(0, challengesRepository
                .findIncomingChallengesByOpponentLoginInput(toNegative, "login_test",  PageRequest.of(0, 1000))
                .getNumberOfElements()
        );

        Page<Challenge> actualResult;
        List<Challenge> expectedResult;
        for (String input: List.of("login", "test")) {
            for (String toLogin : List.of("login", "test")) {
                User to = allCreatedUsers.stream()
                        .filter(u -> u.getLogin().matches(toLogin + "[0-9]$"))
                        .findAny()
                        .get();
                actualResult = challengesRepository.findIncomingChallengesByOpponentLoginInput(
                        to, input, PageRequest.of(0, 1000)
                );
                expectedResult = allCreatedChallenges.stream()
                        .filter(c -> c.getTo().equals(to) && c.getFrom().getLogin().contains(input))
                        .sorted(Comparator.comparing(Challenge::getTimestamp).reversed())
                        .collect(Collectors.toList());
                assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
            }
        }

        User to = allCreatedUsers.stream()
                .filter(u -> u.getLogin().matches("login[0-9]$"))
                .findAny()
                .get();
        actualResult = challengesRepository
                .findIncomingChallengesByOpponentLoginInput(to, "test", PageRequest.of(0, 3));
        expectedResult = allCreatedChallenges.stream()
                .filter(c -> c.getTo().equals(to) && c.getFrom().getLogin().contains("test"))
                .sorted(Comparator.comparing(Challenge::getTimestamp).reversed())
                .limit(3)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        actualResult = challengesRepository
                .findIncomingChallengesByOpponentLoginInput(to, "test", PageRequest.of(2, 3));
        expectedResult = allCreatedChallenges.stream()
                .filter(c -> c.getTo().equals(to) && c.getFrom().getLogin().contains("test"))
                .sorted(Comparator.comparing(Challenge::getTimestamp).reversed())
                .skip(6)
                .limit(3)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));

        assertEquals(0, challengesRepository
                .findIncomingChallengesByOpponentLoginInput(to, "test",  PageRequest.of(100, 10))
                .getNumberOfElements()
        );

        actualResult = challengesRepository
                .findIncomingChallengesByOpponentLoginInput(to, "test", PageRequest.of(0, 5));
        expectedResult = allCreatedChallenges.stream()
                .filter(c -> c.getTo().equals(to) && c.getFrom().getLogin().contains("test"))
                .sorted(Comparator.comparing(Challenge::getTimestamp).reversed())
                .limit(5)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));
        actualResult = challengesRepository
                .findIncomingChallengesByOpponentLoginInput(to, "test", PageRequest.of(2, 5));
        expectedResult = allCreatedChallenges.stream()
                .filter(c -> c.getTo().equals(to) && c.getFrom().getLogin().contains("test"))
                .sorted(Comparator.comparing(Challenge::getTimestamp).reversed())
                .skip(10)
                .limit(5)
                .collect(Collectors.toList());
        assertEquals(expectedResult, actualResult.stream().collect(Collectors.toList()));

    }
}