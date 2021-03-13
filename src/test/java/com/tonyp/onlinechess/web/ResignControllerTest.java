package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Game;
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

import java.util.Optional;

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
    private UsersRepository usersRepository;

    @Autowired
    private GamesRepository gamesRepository;

    @Test
    public void testResignIsNotLoggedIn() throws Exception {
        mvc.perform(post("/resign")
                .param("game_id", "1")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(usersRepository, gamesRepository);
    }

    @Test
    public void testResignForWhite() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        when(gamesRepository.findById(eq(1))).thenReturn(Optional.of(game));
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(white);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/resign")
                .param("game_id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/game?id={id}&resignation={resignation}", "1", "true"));
        verify(gamesRepository, times(1)).findById(1);
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(gamesRepository, times(1)).updateGame(
                game, true, Result.BLACK_WON_BY_RESIGNATION.getDescription()
        );
        verify(usersRepository, times(1)).updateRating(white, -10.0);
        verify(usersRepository, times(1)).updateRating(black, 10.0);
    }

    @Test
    public void testResignForBlack() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        when(gamesRepository.findById(eq(1))).thenReturn(Optional.of(game));
        when(usersRepository.findByLogin(eq("login1"))).thenReturn(black);

        UserSession userSession = new UserSession();
        userSession.setLogin("login1");
        mvc.perform(post("/resign")
                .param("game_id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/game?id={id}&resignation={resignation}", "1", "true"));
        verify(gamesRepository, times(1)).findById(1);
        verify(usersRepository, times(1)).findByLogin("login1");
        verify(gamesRepository, times(1)).updateGame(
                game, true, Result.WHITE_WON_BY_RESIGNATION.getDescription()
        );
        verify(usersRepository, times(1)).updateRating(white, 10.0);
        verify(usersRepository, times(1)).updateRating(black, -10.0);
    }

    @Test
    public void testResignError() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        when(gamesRepository.findById(eq(1))).thenReturn(Optional.of(game));
        when(usersRepository.findByLogin(eq("login0"))).thenReturn(white);
        when(gamesRepository.updateGame(
                eq(game), eq(true), eq(Result.BLACK_WON_BY_RESIGNATION.getDescription())
        )).thenThrow(RuntimeException.class);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/resign")
                .param("game_id", "1")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/game?id={id}&error={error}", "1", "true"));
        verify(gamesRepository, times(1)).findById(1);
        verify(usersRepository, times(1)).findByLogin("login0");
        verify(gamesRepository, times(1)).updateGame(
                game, true, Result.BLACK_WON_BY_RESIGNATION.getDescription()
        );
        verify(usersRepository, times(0)).updateRating(any(User.class), anyDouble());
    }

}