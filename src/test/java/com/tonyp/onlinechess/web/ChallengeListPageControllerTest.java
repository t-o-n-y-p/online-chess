package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesDao;
import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tonyp.onlinechess.web.ChallengeListPageController.COLUMNS;
import static com.tonyp.onlinechess.web.ChallengeListPageController.PAGE_RESULTS;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class ChallengeListPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private ChallengesDao challengesDao;

    @Autowired
    private EntityTransaction tx;

    @Test
    public void testChallengesNotLoggedIn() throws Exception {
        mvc.perform(get("/challenges"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(manager, usersDao, challengesDao, tx);
    }

    @Test
    public void testChallengesFirstPageDefault() throws Exception {
        User user = new User("login0", "pass0");
        User sideUser = new User("login1", "pass1");
        List<Challenge> challenges = IntStream.range(0, PAGE_RESULTS + 1)
                .mapToObj(i -> new Challenge(sideUser, user, Color.WHITE))
                .collect(Collectors.toList());
        AtomicInteger counter = new AtomicInteger(0);
        Map<Integer, List<Challenge>> challengesMap = challenges.stream()
                .limit(PAGE_RESULTS)
                .collect(Collectors.groupingBy(i -> counter.getAndIncrement() % COLUMNS));
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);
        when(challengesDao.findIncomingChallengesByOpponentLoginInput(
                eq(user), eq(""), eq(0), eq(PAGE_RESULTS + 1)
        )).thenReturn(challenges);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenges")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeAccepted", false))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("columns", COLUMNS))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("challengesMap", challengesMap))
                .andExpect(model().attribute("nextPageAvailable", true))
                .andExpect(model().attribute("toPreviousPage", false));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(challengesDao, times(1)).findIncomingChallengesByOpponentLoginInput(
                user, "", 0, PAGE_RESULTS + 1
        );
    }

    @Test
    public void testChallengesFirstPageNoResults() throws Exception {
        User user = new User("login0", "pass0");
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenges")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeAccepted", false))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("columns", COLUMNS))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("challengesMap", Collections.emptyMap()))
                .andExpect(model().attribute("nextPageAvailable", false))
                .andExpect(model().attribute("toPreviousPage", false));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(challengesDao, times(1)).findIncomingChallengesByOpponentLoginInput(
                user, "", 0, PAGE_RESULTS + 1
        );
    }

    @Test
    public void testChallengesFirstPageSingleResultWithAcceptedFlag() throws Exception {
        User user = new User("login0", "pass0");
        User sideUser = new User("login1", "pass1");
        List<Challenge> challenges = List.of(new Challenge(sideUser, user, Color.WHITE));
        Map<Integer, List<Challenge>> challengesMap = Map.of(0, challenges);
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);
        when(challengesDao.findIncomingChallengesByOpponentLoginInput(
                eq(user), eq(""), eq(0), eq(PAGE_RESULTS + 1)
        )).thenReturn(challenges);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenges")
                .param("challenge_accepted", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeAccepted", true))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("columns", COLUMNS))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("challengesMap", challengesMap))
                .andExpect(model().attribute("nextPageAvailable", false))
                .andExpect(model().attribute("toPreviousPage", false));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(challengesDao, times(1)).findIncomingChallengesByOpponentLoginInput(
                user, "", 0, PAGE_RESULTS + 1
        );
    }

    @Test
    public void testChallengesLastPageWithErrorFlag() throws Exception {
        User user = new User("login0", "pass0");
        User sideUser = new User("login1", "pass1");
        List<Challenge> challenges = List.of(new Challenge(sideUser, user, Color.WHITE));
        Map<Integer, List<Challenge>> challengesMap = Map.of(0, challenges);
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);
        when(challengesDao.findIncomingChallengesByOpponentLoginInput(
                eq(user), eq("qwerty"), eq(3 * PAGE_RESULTS), eq(PAGE_RESULTS + 1)
        )).thenReturn(challenges);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenges")
                .param("page", "4")
                .param("search", "qwerty")
                .param("error", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeAccepted", false))
                .andExpect(model().attribute("error", true))
                .andExpect(model().attribute("search", "qwerty"))
                .andExpect(model().attribute("page", 4))
                .andExpect(model().attribute("columns", COLUMNS))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("challengesMap", challengesMap))
                .andExpect(model().attribute("nextPageAvailable", false))
                .andExpect(model().attribute("toPreviousPage", true));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(challengesDao, times(1)).findIncomingChallengesByOpponentLoginInput(
                user, "qwerty", 3 * PAGE_RESULTS, PAGE_RESULTS + 1
        );
    }
}