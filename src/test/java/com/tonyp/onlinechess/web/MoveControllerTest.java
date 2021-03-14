package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.MovesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
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

import java.util.Optional;

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
    private UsersRepository usersRepository;

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private MovesRepository movesRepository;

    @Test
    public void testMakeMoveIsNotLoggedIn() throws Exception {
        mvc.perform(post("/move")
                .param("game_id", "1")
                .param("square1", "e2")
                .param("square2", "e4")
                .param("promotion", "")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/login?force_logout={forceLogout}", "true"));
        verifyNoInteractions(usersRepository, gamesRepository, movesRepository);
    }

    @Test
    public void testMakeMoveLegalMoveGameNotFinished() throws Exception {
        String newFen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        Move move = new Move(game, "e2e4", newFen);
        when(gamesRepository.findById(eq(1))).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(eq(game), eq("e2e4"), eq(newFen))).thenReturn(move);

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

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, times(1)).createNewMove(game, "e2e4", newFen);
        verify(gamesRepository, times(1)).updateGame(
                game, newFen,
                "a7a6 b7b6 c7c6 d7d6 e7e6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 e7e5 f7f5 g7g5 h7h5 b8a6 b8c6 g8f6 g8h6 ",
                false, null, move
        );
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testMakeMoveLegalMoveGameFinished() throws Exception {
        String newFen = "rnb1kbnr/pppp1ppp/8/4p3/5PPq/8/PPPPP2P/RNBQKBNR w KQkq - 1 3";

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        game.setFen("rnbqkbnr/pppp1ppp/8/4p3/5PP1/8/PPPPP2P/RNBQKBNR b KQkq - 0 2");
        game.setLegalMoves("d8h4");
        game.setPlayerToMove(black);
        Move move = new Move(game, "d8h4", newFen);
        when(gamesRepository.findById(eq(1))).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(eq(game), eq("d8h4"), eq(newFen))).thenReturn(move);

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

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, times(1)).createNewMove(game, "d8h4", newFen);
        verify(gamesRepository, times(1)).updateGame(
                game, newFen,
                "",
                true, Result.BLACK_WON_BY_CHECKMATE.getDescription(), move
        );
        verify(usersRepository, times(1)).updateRating(white, -10.0);
        verify(usersRepository, times(1)).updateRating(black, 10.0);
    }

    @Test
    public void testMakeMoveIllegalMove() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        Move move = new Move(game, "a1a1", "qwerty");
        when(gamesRepository.findById(eq(1))).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(eq(game), eq("a1a1"), anyString())).thenReturn(move);

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

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, never()).createNewMove(any(Game.class), anyString(), anyString());
        verify(gamesRepository, never()).updateGame(
                any(Game.class), anyString(), anyString(), anyBoolean(), anyString(), any(Move.class)
        );
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testMakeMoveError() throws Exception {
        String newFen = "rnb1kbnr/pppp1ppp/8/4p3/5PPq/8/PPPPP2P/RNBQKBNR w KQkq - 1 3";

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        Move move = new Move(game, "e2e4", newFen);
        when(gamesRepository.findById(eq(1))).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(eq(game), eq("e2e4"), eq(newFen))).thenReturn(move);
        when(gamesRepository.updateGame(
                eq(game), eq(newFen),
                eq("a7a6 b7b6 c7c6 d7d6 e7e6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 e7e5 f7f5 g7g5 h7h5 b8a6 b8c6 g8f6 g8h6 "),
                eq(false), eq(null), eq(move))
        ).thenThrow(RuntimeException.class);

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

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, times(1)).createNewMove(game, "e2e4", newFen);
        verify(gamesRepository, times(1)).updateGame(
                game, newFen,
                "a7a6 b7b6 c7c6 d7d6 e7e6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 e7e5 f7f5 g7g5 h7h5 b8a6 b8c6 g8f6 g8h6 ",
                false, null, move
        );
        verifyNoInteractions(usersRepository);
    }
}