package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.MovesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.GameUtil;
import com.tonyp.onlinechess.tools.Result;
import com.tonyp.onlinechess.tools.StockfishUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class MoveControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager manager;

    @MockBean
    private UsersDao usersDao;

    @MockBean
    private GamesDao gamesDao;

    @MockBean
    private MovesDao movesDao;

    @Autowired
    private EntityTransaction tx;

    @Test
    public void testIsNotLoggedIn() throws Exception {
        mvc.perform(post("/move")
                .param("game_id", "1")
                .param("square1", "e2")
                .param("square2", "e4")
                .param("promotion", "")
                .sessionAttr("user-session", new UserSession())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(manager, usersDao, gamesDao, movesDao, tx);
    }

    @Test
    public void testLegalMoveGameNotFinished() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        Move move = new Move(game, "e2e4");
        when(manager.find(eq(Game.class), eq(1))).thenReturn(game);
        when(manager.getTransaction()).thenReturn(tx);
        when(movesDao.createNewMove(eq(game), eq("e2e4"))).thenReturn(move);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/move")
                .param("game_id", "1")
                .param("square1", "e2")
                .param("square2", "e4")
                .param("promotion", "")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/game?id={id}&legal_move={legalMove}", "1", "true"));

        verify(manager, times(1)).find(Game.class, 1);
        verify(movesDao, times(1)).createNewMove(game, "e2e4");
        verify(gamesDao, times(1)).updateGame(
                game,
                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
                "a7a6 b7b6 c7c6 d7d6 e7e6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 e7e5 f7f5 g7g5 h7h5 b8a6 b8c6 g8f6 g8h6 ",
                false, null, move
        );

        verify(manager, times(3)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
    }

    @Test
    public void testLegalMoveGameFinished() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        game.setFen("rnbqkbnr/pppp1ppp/8/4p3/5PP1/8/PPPPP2P/RNBQKBNR b KQkq - 0 2");
        game.setLegalMoves("d8h4");
        game.setPlayerToMove(black);
        Move move = new Move(game, "d8h4");
        when(manager.find(eq(Game.class), eq(1))).thenReturn(game);
        when(manager.getTransaction()).thenReturn(tx);
        when(movesDao.createNewMove(eq(game), eq("d8h4"))).thenReturn(move);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/move")
                .param("game_id", "1")
                .param("square1", "d8")
                .param("square2", "h4")
                .param("promotion", "")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/game?id={id}&legal_move={legalMove}", "1", "true"));

        verify(manager, times(1)).find(Game.class, 1);
        verify(movesDao, times(1)).createNewMove(game, "d8h4");
        verify(gamesDao, times(1)).updateGame(
                game,
                "rnb1kbnr/pppp1ppp/8/4p3/5PPq/8/PPPPP2P/RNBQKBNR w KQkq - 1 3",
                "",
                true, Result.BLACK_WON_BY_CHECKMATE.getDescription(), move
        );
        verify(usersDao, times(1)).updateRating(white, -10.0);
        verify(usersDao, times(1)).updateRating(black, 10.0);

        verify(manager, times(3)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
    }

    @Test
    public void testIllegalMove() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        Move move = new Move(game, "a1a1");
        when(manager.find(eq(Game.class), eq(1))).thenReturn(game);
        when(manager.getTransaction()).thenReturn(tx);
        when(movesDao.createNewMove(eq(game), eq("a1a1"))).thenReturn(move);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/move")
                .param("game_id", "1")
                .param("square1", "a1")
                .param("square2", "a1")
                .param("promotion", "")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/game?id={id}&illegal_move={illegalMove}", "1", "true"));

        verify(manager, times(1)).find(Game.class, 1);
        verify(movesDao, never()).createNewMove(any(Game.class), anyString());
        verify(gamesDao, never()).updateGame(
                any(Game.class), anyString(), anyString(), anyBoolean(), anyString(), any(Move.class)
        );

        verify(manager, times(1)).getTransaction();
        verify(tx, never()).begin();
        verify(tx, never()).commit();
        verify(tx, times(1)).isActive();
    }

    @Test
    public void testError() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        Move move = new Move(game, "e2e4");
        when(manager.find(eq(Game.class), eq(1))).thenReturn(game);
        when(manager.getTransaction()).thenReturn(tx);
        doThrow(RuntimeException.class).when(tx).commit();
        when(tx.isActive()).thenReturn(true);
        when(movesDao.createNewMove(eq(game), eq("e2e4"))).thenReturn(move);

        UserSession userSession = new UserSession();
        userSession.setLogin("login0");
        mvc.perform(post("/move")
                .param("game_id", "1")
                .param("square1", "e2")
                .param("square2", "e4")
                .param("promotion", "")
                .sessionAttr("user-session", userSession)
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/game?id={id}&error={error}", "1", "true"));

        verify(manager, times(1)).find(Game.class, 1);
        verify(movesDao, times(1)).createNewMove(game, "e2e4");
        verify(gamesDao, times(1)).updateGame(
                game,
                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
                "a7a6 b7b6 c7c6 d7d6 e7e6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 e7e5 f7f5 g7g5 h7h5 b8a6 b8c6 g8f6 g8h6 ",
                false, null, move
        );

        verify(manager, times(4)).getTransaction();
        verify(tx, times(1)).begin();
        verify(tx, times(1)).commit();
        verify(tx, times(1)).isActive();
        verify(tx, times(1)).rollback();
    }
}