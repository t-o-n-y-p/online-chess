package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class GamePageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private GamesRepository gamesRepository;

    @Test
    public void testGameLegalMoveAsWhite() throws Exception {
        User user = new User("login0", "pass0");
        Game game = new Game(user, new User("login1", "pass1"));
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(usersRepository.findByLogin("login0")).thenReturn(user);

        mvc.perform(get("/app/game")
                .with(user("login0"))
                .param("id", "1")
                .param("legal_move", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("legalMove", true))
                .andExpect(model().attribute("illegalMove", false))
                .andExpect(model().attribute("resignation", false))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("game", game))
                .andExpect(model().attribute("board", GameUtil.getBoard(game.getFen(), Color.WHITE)))
                .andExpect(model().attribute("squares", GameUtil.SQUARES));
        verify(gamesRepository, times(1)).findById(1);
        verify(usersRepository, times(1)).findByLogin("login0");
    }

    @Test
    public void testGameIllegalMoveAsBlack() throws Exception {
        User user = new User("login0", "pass0");
        Game game = new Game(new User("login1", "pass1"), user);
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(usersRepository.findByLogin("login0")).thenReturn(user);

        mvc.perform(get("/app/game")
                .with(user("login0"))
                .param("id", "1")
                .param("illegal_move", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("legalMove", false))
                .andExpect(model().attribute("illegalMove", true))
                .andExpect(model().attribute("resignation", false))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("game", game))
                .andExpect(model().attribute("board", GameUtil.getBoard(game.getFen(), Color.BLACK)))
                .andExpect(model().attribute("squares", GameUtil.SQUARES));
        verify(gamesRepository, times(1)).findById(1);
        verify(usersRepository, times(1)).findByLogin("login0");
    }

    @Test
    public void testGameResignation() throws Exception {
        User user = new User("login0", "pass0");
        Game game = new Game(user, new User("login1", "pass1"));
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(usersRepository.findByLogin("login0")).thenReturn(user);

        mvc.perform(get("/app/game")
                .with(user("login0"))
                .param("id", "1")
                .param("resignation", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("legalMove", false))
                .andExpect(model().attribute("illegalMove", false))
                .andExpect(model().attribute("resignation", true))
                .andExpect(model().attribute("error", false))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("game", game))
                .andExpect(model().attribute("board", GameUtil.getBoard(game.getFen(), Color.WHITE)))
                .andExpect(model().attribute("squares", GameUtil.SQUARES));
        verify(gamesRepository, times(1)).findById(1);
        verify(usersRepository, times(1)).findByLogin("login0");
    }

    @Test
    public void testGameError() throws Exception {
        User user = new User("login0", "pass0");
        Game game = new Game(user, new User("login1", "pass1"));
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(usersRepository.findByLogin("login0")).thenReturn(user);

        mvc.perform(get("/app/game")
                .with(user("login0"))
                .param("id", "1")
                .param("error", "true")
        )
                .andExpect(status().isOk())
                .andExpect(model().attribute("legalMove", false))
                .andExpect(model().attribute("illegalMove", false))
                .andExpect(model().attribute("resignation", false))
                .andExpect(model().attribute("error", true))
                .andExpect(model().attribute("user", user))
                .andExpect(model().attribute("game", game))
                .andExpect(model().attribute("board", GameUtil.getBoard(game.getFen(), Color.WHITE)))
                .andExpect(model().attribute("squares", GameUtil.SQUARES));
        verify(gamesRepository, times(1)).findById(1);
        verify(usersRepository, times(1)).findByLogin("login0");
    }

}