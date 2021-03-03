package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.GameUtil;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class GamePageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager manager;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private EntityTransaction tx;

    @Test
    public void testGameNotLoggedIn() throws Exception {
        mvc.perform(get("/game")
                .param("id", "1")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(manager, usersDao, tx);
    }

    @Test
    public void testGameLegalMoveAsWhite() throws Exception {
        User user = new User("login0", "pass0");
        Game game = new Game(user, new User("login1", "pass1"));
        when(manager.find(eq(Game.class), eq(1))).thenReturn(game);
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/game")
                .param("id", "1")
                .param("legal_move", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("legalMove", true))
                .andExpect(model().attribute("illegalMove", false))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("game", game))
                .andExpect(model().attribute("board", GameUtil.getBoard(game.getFen(), Color.WHITE)))
                .andExpect(model().attribute("squares", GameUtil.SQUARES));
        verify(manager, times(1)).find(Game.class, 1);
        verify(usersDao, times(1)).findByLogin("login0");
    }

    @Test
    public void testGameIllegalMoveAsBlack() throws Exception {
        User user = new User("login0", "pass0");
        user.setId(1);
        Game game = new Game(new User("login1", "pass1"), user);
        when(manager.find(eq(Game.class), eq(1))).thenReturn(game);
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/game")
                .param("id", "1")
                .param("illegal_move", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("legalMove", false))
                .andExpect(model().attribute("illegalMove", true))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("game", game))
                .andExpect(model().attribute("board", GameUtil.getBoard(game.getFen(), Color.BLACK)))
                .andExpect(model().attribute("squares", GameUtil.SQUARES));
        verify(manager, times(1)).find(Game.class, 1);
        verify(usersDao, times(1)).findByLogin("login0");
    }

    @Test
    public void testGameError() throws Exception {
        User user = new User("login0", "pass0");
        Game game = new Game(user, new User("login1", "pass1"));
        when(manager.find(eq(Game.class), eq(1))).thenReturn(game);
        when(usersDao.findByLogin(eq("login0"))).thenReturn(user);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(get("/game")
                .param("id", "1")
                .param("error", "true")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("legalMove", false))
                .andExpect(model().attribute("illegalMove", false))
                .andExpect(model().attribute("error", true))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("game", game))
                .andExpect(model().attribute("board", GameUtil.getBoard(game.getFen(), Color.WHITE)))
                .andExpect(model().attribute("squares", GameUtil.SQUARES));
        verify(manager, times(1)).find(Game.class, 1);
        verify(usersDao, times(1)).findByLogin("login0");
    }

}