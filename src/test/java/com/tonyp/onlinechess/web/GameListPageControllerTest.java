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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tonyp.onlinechess.web.GameListPageController.COLUMNS;
import static com.tonyp.onlinechess.web.GameListPageController.PAGE_RESULTS;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class GameListPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private GamesDao gamesDao;

    @Test
    public void testGamesNotLoggedIn() throws Exception {
        mvc.perform(get("/games"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(manager, usersDao, gamesDao);
    }

    @Test
    public void testGamesFirstPageDefault() throws Exception {
        User user = new User("login0", "pass0");
        User sideUser = new User("login1", "pass1");
        List<Game> games = IntStream.range(0, PAGE_RESULTS + 1)
                .mapToObj(i -> new Game(sideUser, user))
                .collect(Collectors.toList());
        AtomicInteger counter = new AtomicInteger(0);
        Map<Integer, List<Game>> gamesMap = games.stream()
                .limit(PAGE_RESULTS)
                .collect(Collectors.groupingBy(i -> counter.getAndIncrement() % COLUMNS));
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);
        when(gamesDao.findByUserAndOpponentLoginInput(
                eq(user), eq(""), eq(0), eq(PAGE_RESULTS + 1)
        )).thenReturn(games);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/games")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("columns", COLUMNS))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("gamesMap", gamesMap))
                .andExpect(model().attribute("nextPageAvailable", true));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(gamesDao, times(1)).findByUserAndOpponentLoginInput(
                user, "", 0, PAGE_RESULTS + 1
        );
    }

    @Test
    public void testGamesFirstPageNoResults() throws Exception {
        User user = new User("login0", "pass0");
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/games")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("columns", COLUMNS))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("gamesMap", Collections.emptyMap()))
                .andExpect(model().attribute("nextPageAvailable", false));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(gamesDao, times(1)).findByUserAndOpponentLoginInput(
                user, "", 0, PAGE_RESULTS + 1
        );
    }

    @Test
    public void testGamesLastPageWithSearch() throws Exception {
        User user = new User("login0", "pass0");
        User sideUser = new User("login1", "pass1");
        List<Game> games = List.of(new Game(sideUser, user));
        Map<Integer, List<Game>> gamesMap = Map.of(0, games);
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);
        when(gamesDao.findByUserAndOpponentLoginInput(
                eq(user), eq("qwerty"), eq(3 * PAGE_RESULTS), eq(PAGE_RESULTS + 1)
        )).thenReturn(games);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/games")
                .param("page", "4")
                .param("search", "qwerty")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", "qwerty"))
                .andExpect(model().attribute("page", 4))
                .andExpect(model().attribute("columns", COLUMNS))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("gamesMap", gamesMap))
                .andExpect(model().attribute("nextPageAvailable", false));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(gamesDao, times(1)).findByUserAndOpponentLoginInput(
                user, "qwerty", 3 * PAGE_RESULTS, PAGE_RESULTS + 1
        );
    }

}