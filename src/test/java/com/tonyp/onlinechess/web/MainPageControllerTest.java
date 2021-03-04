package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesDao;
import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.Game;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tonyp.onlinechess.web.MainPageController.MAIN_PAGE_RESULTS;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class MainPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private ChallengesDao challengesDao;

    @Autowired
    private GamesDao gamesDao;

    @Test
    public void testMainNotLoggedIn() throws Exception {
        mvc.perform(get("/main"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(manager, usersDao, challengesDao, gamesDao);
    }

    @Test
    public void testMainChallengeCreatedFewChallengesEmptyGames() throws Exception {
        User user = new User("login0", "pass0");
        User sideUser = new User("login1", "pass1");
        List<Challenge> challenges = List.of(new Challenge(sideUser, user, Color.WHITE));
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);
        when(challengesDao.findIncomingChallenges(eq(user), eq(0), eq(MAIN_PAGE_RESULTS + 1))).thenReturn(challenges);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/main")
                .param("challenge_created", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeCreated", true))
                .andExpect(model().attribute("challengeAccepted", false))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("incomingChallenges", challenges))
                .andExpect(model().attribute("canViewAllChallenges", false))
                .andExpect(model().attribute("games", Collections.emptyList()))
                .andExpect(model().attribute("canViewAllGames", false));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(challengesDao, times(1))
                .findIncomingChallenges(user, 0, MAIN_PAGE_RESULTS + 1);
        verify(gamesDao, times(1))
                .findByUser(user, 0, MAIN_PAGE_RESULTS + 1);
    }

    @Test
    public void testMainChallengeAcceptedEmptyChallengesManyGames() throws Exception {
        User user = new User("login0", "pass0");
        User sideUser = new User("login1", "pass1");
        List<Game> games = IntStream.range(0, MAIN_PAGE_RESULTS + 1)
                .mapToObj(i -> new Game(sideUser, user))
                .collect(Collectors.toList());
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);
        when(gamesDao.findByUser(eq(user), eq(0), eq(MAIN_PAGE_RESULTS + 1))).thenReturn(games);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/main")
                .param("challenge_accepted", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeCreated", false))
                .andExpect(model().attribute("challengeAccepted", true))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("incomingChallenges", Collections.emptyList()))
                .andExpect(model().attribute("canViewAllChallenges", false))
                .andExpect(model().attribute("games", games.subList(0, MAIN_PAGE_RESULTS)))
                .andExpect(model().attribute("canViewAllGames", true));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(challengesDao, times(1))
                .findIncomingChallenges(user, 0, MAIN_PAGE_RESULTS + 1);
        verify(gamesDao, times(1))
                .findByUser(user, 0, MAIN_PAGE_RESULTS + 1);
    }

    @Test
    public void testMainErrorManyChallengesFewGames() throws Exception {
        User user = new User("login0", "pass0");
        User sideUser = new User("login1", "pass1");
        List<Challenge> challenges = IntStream.range(0, MAIN_PAGE_RESULTS + 1)
                .mapToObj(i -> new Challenge(sideUser, user, Color.WHITE))
                .collect(Collectors.toList());
        List<Game> games = List.of(new Game(sideUser, user));
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);
        when(challengesDao.findIncomingChallenges(eq(user), eq(0), eq(MAIN_PAGE_RESULTS + 1))).thenReturn(challenges);
        when(gamesDao.findByUser(eq(user), eq(0), eq(MAIN_PAGE_RESULTS + 1))).thenReturn(games);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/main")
                .param("error", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("challengeCreated", false))
                .andExpect(model().attribute("challengeAccepted", false))
                .andExpect(model().attribute("error", true))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("incomingChallenges", challenges.subList(0, MAIN_PAGE_RESULTS)))
                .andExpect(model().attribute("canViewAllChallenges", true))
                .andExpect(model().attribute("games", games))
                .andExpect(model().attribute("canViewAllGames", false));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(challengesDao, times(1))
                .findIncomingChallenges(user, 0, MAIN_PAGE_RESULTS + 1);
        verify(gamesDao, times(1))
                .findByUser(user, 0, MAIN_PAGE_RESULTS + 1);
    }

}