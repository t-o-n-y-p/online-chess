package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesDao;
import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.MovesDao;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlTemplate;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class ChallengeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private GamesDao gamesDao;

    @Autowired
    private ChallengesDao challengesDao;

    @Autowired
    private EntityTransaction tx;

    @Test
    public void testAcceptIsNotLoggedIn() throws Exception {
        mvc.perform(post("/challenge/accept")
                .param("id", "1")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("../login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(manager, usersDao, gamesDao, challengesDao, tx);
    }

    @Test
    public void testAcceptFromMainWhite() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        Challenge challenge = new Challenge(from, to, Color.WHITE);
        when(manager.find(eq(Challenge.class), eq(1))).thenReturn(challenge);
        when(manager.getTransaction()).thenReturn(tx);

        UserSession userSession = new UserSession();
        userSession.setLogin("login1");
        mvc.perform(post("/challenge/accept")
                .param("id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/main?challenge_accepted={accepted}", "true"));

        verify(manager, times(1)).find(Challenge.class, 1);
        verify(manager, times(1)).remove(challenge);
        verify(gamesDao, times(1)).createNewGame(to, from);
        verify(manager, times(3)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
    }

    @Test
    public void testAcceptFromChallengesBlack() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        Challenge challenge = new Challenge(from, to, Color.BLACK);
        when(manager.find(eq(Challenge.class), eq(1))).thenReturn(challenge);
        when(manager.getTransaction()).thenReturn(tx);

        UserSession userSession = new UserSession();
        userSession.setLogin("login1");
        mvc.perform(post("/challenge/accept")
                .param("id", "1")
                .param("from_challenges", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate(
                        "/challenges?challenge_accepted={accepted}&page={page}", "true", "1"));

        verify(manager, times(1)).find(Challenge.class, 1);
        verify(manager, times(1)).remove(challenge);
        verify(gamesDao, times(1)).createNewGame(from, to);
        verify(manager, times(3)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
    }

    @Test
    public void testAcceptFromChallengesToPreviousPageWhite() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        Challenge challenge = new Challenge(from, to, Color.WHITE);
        when(manager.find(eq(Challenge.class), eq(1))).thenReturn(challenge);
        when(manager.getTransaction()).thenReturn(tx);

        UserSession userSession = new UserSession();
        userSession.setLogin("login1");
        mvc.perform(post("/challenge/accept")
                .param("id", "1")
                .param("page", "3")
                .param("to_previous_page", "true")
                .param("from_challenges", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate(
                        "/challenges?challenge_accepted={accepted}&page={page}", "true", "2"));

        verify(manager, times(1)).find(Challenge.class, 1);
        verify(manager, times(1)).remove(challenge);
        verify(gamesDao, times(1)).createNewGame(to, from);
        verify(manager, times(3)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
    }

    @Test
    public void testAcceptErrorBlack() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        Challenge challenge = new Challenge(from, to, Color.BLACK);
        when(manager.find(eq(Challenge.class), eq(1))).thenReturn(challenge);
        when(manager.getTransaction()).thenReturn(tx);
        doThrow(RuntimeException.class).when(tx).commit();
        when(tx.isActive()).thenReturn(true);

        UserSession userSession = new UserSession();
        userSession.setLogin("login1");
        mvc.perform(post("/challenge/accept")
                .param("id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/main?error={error}", "true"));

        verify(manager, times(1)).find(Challenge.class, 1);
        verify(manager, times(1)).remove(challenge);
        verify(gamesDao, times(1)).createNewGame(from, to);
        verify(manager, times(4)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
        verify(tx, times(1)).rollback();
    }

    @Test
    public void testCreateIsNotLoggedIn() throws Exception {
        mvc.perform(post("/challenge")
                .param("opponent_id", "1")
                .param("target_color", Color.WHITE.name())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(manager, usersDao, gamesDao, challengesDao, tx);
    }

    @Test
    public void testCreateSuccessWhite() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        when(usersDao.findByLogin(eq("login0"))).thenReturn(from);
        when(manager.find(eq(User.class), eq(1))).thenReturn(to);
        when(manager.getTransaction()).thenReturn(tx);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/challenge")
                .param("opponent_id", "1")
                .param("target_color", Color.WHITE.name())
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/main?challenge_created={created}", "true"));
        verify(manager, times(1)).find(User.class, 1);
        verify(challengesDao, times(1)).createNewChallenge(from, to, Color.WHITE);
        verify(usersDao, times(1)).findByLogin("login0");
        verify(manager, times(3)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
    }

    @Test
    public void testCreateErrorBlack() throws Exception {
        User from = new User("login0", "pass0");
        User to = new User("login1", "pass1");
        when(usersDao.findByLogin(eq("login0"))).thenReturn(from);
        when(manager.find(eq(User.class), eq(1))).thenReturn(to);
        when(manager.getTransaction()).thenReturn(tx);
        doThrow(RuntimeException.class).when(tx).commit();
        when(tx.isActive()).thenReturn(true);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/challenge")
                .param("opponent_id", "1")
                .param("target_color", Color.BLACK.name())
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/main?error={error}", "true"));
        verify(manager, times(1)).find(User.class, 1);
        verify(challengesDao, times(1)).createNewChallenge(from, to, Color.BLACK);
        verify(usersDao, times(1)).findByLogin("login0");
        verify(manager, times(4)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
        verify(tx, times(1)).rollback();
    }
}