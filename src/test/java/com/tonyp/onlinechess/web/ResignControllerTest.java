package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
public class ResignControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private GamesDao gamesDao;

    @Autowired
    private EntityTransaction tx;

    @Test
    public void testResignIsNotLoggedIn() throws Exception {
        mvc.perform(post("/resign")
                .param("game_id", "1")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(manager, usersDao, gamesDao, tx);
    }

    @Test
    public void testResignForWhite() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        when(manager.find(eq(Game.class), eq(1))).thenReturn(game);
        when(usersDao.findByLogin(eq("login0"))).thenReturn(white);
        when(manager.getTransaction()).thenReturn(tx);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/resign")
                .param("game_id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/game?id={id}&resignation={resignation}", "1", "true"));
        verify(manager, times(1)).find(Game.class, 1);
        verify(usersDao, times(1)).findByLogin("login0");
        verify(gamesDao, times(1)).updateGame(
                game, true, Result.BLACK_WON_BY_RESIGNATION.getDescription()
        );
        verify(usersDao, times(1)).updateRating(white, -10.0);
        verify(usersDao, times(1)).updateRating(black, 10.0);

        verify(manager, times(3)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
    }

    @Test
    public void testResignForBlack() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        when(manager.find(eq(Game.class), eq(1))).thenReturn(game);
        when(usersDao.findByLogin(eq("login1"))).thenReturn(black);
        when(manager.getTransaction()).thenReturn(tx);

        UserSession userSession = new UserSession();
        userSession.setLogin("login1");
        mvc.perform(post("/resign")
                .param("game_id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/game?id={id}&resignation={resignation}", "1", "true"));
        verify(manager, times(1)).find(Game.class, 1);
        verify(usersDao, times(1)).findByLogin("login1");
        verify(gamesDao, times(1)).updateGame(
                game, true, Result.WHITE_WON_BY_RESIGNATION.getDescription()
        );
        verify(usersDao, times(1)).updateRating(white, 10.0);
        verify(usersDao, times(1)).updateRating(black, -10.0);

        verify(manager, times(3)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
    }

    @Test
    public void testResignError() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        when(manager.find(eq(Game.class), eq(1))).thenReturn(game);
        when(usersDao.findByLogin(eq("login0"))).thenReturn(white);
        when(manager.getTransaction()).thenReturn(tx);
        doThrow(RuntimeException.class).when(tx).commit();
        when(tx.isActive()).thenReturn(true);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/resign")
                .param("game_id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/game?id={id}&error={error}", "1", "true"));
        verify(manager, times(1)).find(Game.class, 1);
        verify(usersDao, times(1)).findByLogin("login0");
        verify(gamesDao, times(1)).updateGame(
                game, true, Result.BLACK_WON_BY_RESIGNATION.getDescription()
        );
        verify(usersDao, times(1)).updateRating(white, -10.0);
        verify(usersDao, times(1)).updateRating(black, 10.0);

        verify(manager, times(4)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
        verify(tx, times(1)).rollback();
    }

}