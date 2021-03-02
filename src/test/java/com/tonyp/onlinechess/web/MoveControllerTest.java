package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.StockfishUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class MoveControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager manager;

    @Autowired
    private EntityTransaction tx;

    @Test
    public void makeMove() throws Exception {

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        Move move = new Move(game, "e2e4");
        when(manager.find(eq(Game.class), eq("1"))).thenReturn(game);
        when(manager.merge(eq(game))).thenReturn(game);
        when(manager.merge(eq(white))).thenReturn(white);
        when(manager.merge(eq(black))).thenReturn(black);
        when(manager.getTransaction()).thenReturn(tx);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/move")
                .param("game_id", "1")
                .param("square1", "e2")
                .param("square2", "e4")
                .param("promotion", "")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection());

    }
}