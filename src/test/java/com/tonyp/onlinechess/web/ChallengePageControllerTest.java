package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.UsersDao;
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

import static com.tonyp.onlinechess.web.ChallengePageController.*;
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
public class ChallengePageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private EntityTransaction tx;

    @Test
    public void testStep1NotLoggedIn() throws Exception {
        mvc.perform(get("/challenge/step1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(manager, usersDao, tx);
    }

    @Test
    public void testStep1FirstPageDefault() throws Exception {
        User user = new User("login0", "pass0");
        List<User> opponents = IntStream.range(0, PAGE_RESULTS + 1)
                .mapToObj(i -> new User("login" + (i + 1), "pass"))
                .collect(Collectors.toList());
        AtomicInteger counter = new AtomicInteger(0);
        Map<Integer, List<User>> opponentsMap = opponents.stream()
                .limit(PAGE_RESULTS)
                .collect(Collectors.groupingBy(i -> counter.getAndIncrement() % COLUMNS));
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);
        when(usersDao.findOpponentsByRatingAndLoginInput(
                eq(user), eq(""), eq(user.getRating()), eq(RATING_THRESHOLD), eq(0), eq(PAGE_RESULTS + 1)
        )).thenReturn(opponents);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenge/step1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("columns", COLUMNS))
                .andExpect(model().attribute("opponentsMap", opponentsMap))
                .andExpect(model().attribute("nextPageAvailable", true));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(usersDao, times(1)).findOpponentsByRatingAndLoginInput(
                user, "", user.getRating(), RATING_THRESHOLD, 0, PAGE_RESULTS + 1
        );
    }

    @Test
    public void testStep1FirstPageNoResults() throws Exception {
        User user = new User("login0", "pass0");
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenge/step1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("columns", COLUMNS))
                .andExpect(model().attribute("opponentsMap", Collections.emptyMap()))
                .andExpect(model().attribute("nextPageAvailable", false));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(usersDao, times(1)).findOpponentsByRatingAndLoginInput(
                user, "", user.getRating(), RATING_THRESHOLD, 0, PAGE_RESULTS + 1
        );
    }

    @Test
    public void testStep1LastPageWithSearch() throws Exception {
        User user = new User("login0", "pass0");
        List<User> opponents = List.of(new User("login1", "pass1"));
        Map<Integer, List<User>> opponentsMap = Map.of(0, opponents);
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);
        when(usersDao.findOpponentsByRatingAndLoginInput(
                eq(user), eq("qwerty"), eq(user.getRating()), eq(RATING_THRESHOLD),
                eq(3 * PAGE_RESULTS), eq(PAGE_RESULTS + 1)
        )).thenReturn(opponents);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenge/step1")
                .param("page", "4")
                .param("search", "qwerty")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", "qwerty"))
                .andExpect(model().attribute("page", 4))
                .andExpect(model().attribute("columns", COLUMNS))
                .andExpect(model().attribute("opponentsMap", opponentsMap))
                .andExpect(model().attribute("nextPageAvailable", false));
        verify(usersDao, times(1)).findByLogin("login0");
        verify(usersDao, times(1)).findOpponentsByRatingAndLoginInput(
                user, "qwerty", user.getRating(), RATING_THRESHOLD, 3 * PAGE_RESULTS, PAGE_RESULTS + 1
        );
    }

    @Test
    public void testStep2NotLoggedIn() throws Exception {
        mvc.perform(get("/challenge/step2")
                .param("opponent_id", "1")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(manager, usersDao, tx);
    }

    @Test
    public void testStep2Success() throws Exception {
        User opponent = new User("login1", "pass1");
        when(manager.find(eq(User.class), eq(1))).thenReturn(opponent);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/challenge/step2")
                .param("opponent_id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("opponent", opponent));
        verify(manager, times(1)).find(User.class, 1);
    }

}