package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.MovesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.GameUtil;
import com.tonyp.onlinechess.tools.Result;
import com.tonyp.onlinechess.tools.StockfishUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlTemplate;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    public void testMakeMoveLegalMoveGameNotFinished() throws Exception {
        String newFen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        Move move = new Move(game, null, "e2e4", newFen);
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(game, null, "e2e4", newFen)).thenReturn(move);

        mvc.perform(post("/app/move")
                .with(user("login0"))
                .param("game_id", "1")
                .param("square1", "e2")
                .param("square2", "e4")
                .param("promotion", "")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/app/game?id={id}&legal_move={legalMove}", "1", "true"));

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, times(1)).createNewMove(game, null, "e2e4", newFen);
        verify(gamesRepository, times(1)).updateGame(
                game, newFen,
                "a7a6 b7b6 c7c6 d7d6 e7e6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 e7e5 f7f5 g7g5 h7h5 b8a6 b8c6 g8f6 g8h6 ",
                false, null, move
        );
        verify(movesRepository, never()).updateMove(any(Move.class), any(Move.class));
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testMakeMoveLegalMoveBlackWonByCheckmate() throws Exception {
        String newFen = "rnb1kbnr/pppp1ppp/8/4p3/5PPq/8/PPPPP2P/RNBQKBNR w KQkq - 1 3";

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        game.setFen("rnbqkbnr/pppp1ppp/8/4p3/5PP1/8/PPPPP2P/RNBQKBNR b KQkq - 0 2");
        game.setLegalMoves("e5e4 a7a6 b7b6 c7c6 d7d6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 f7f5 g7g5 h7h5 e5f4 b8a6 b8c6 " +
                "g8f6 g8h6 g8e7 f8a3 f8b4 f8c5 f8d6 f8e7 d8h4 d8g5 d8f6 d8e7 e8e7 ");
        game.setPlayerToMove(black);
        Move previousMove = new Move(game, null, "g2g4",
                "rnbqkbnr/pppp1ppp/8/4p3/5PP1/8/PPPPP2P/RNBQKBNR b KQkq - 0 2"
        );
        game.setMoves(List.of(previousMove));
        Move move = new Move(game, previousMove, "d8h4", newFen);
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(game, previousMove, "d8h4", newFen)).thenReturn(move);

        mvc.perform(post("/app/move")
                .with(user("login1"))
                .param("game_id", "1")
                .param("square1", "d8")
                .param("square2", "h4")
                .param("promotion", "")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/app/game?id={id}&legal_move={legalMove}", "1", "true"));

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, times(1)).createNewMove(game, previousMove, "d8h4", newFen);
        verify(gamesRepository, times(1)).updateGame(
                game, newFen,
                "",
                true, Result.BLACK_WON_BY_CHECKMATE.getDescription(), move
        );
        verify(movesRepository, times(1)).updateMove(previousMove, move);
        verify(usersRepository, times(1)).updateRating(white, -10.0);
        verify(usersRepository, times(1)).updateRating(black, 10.0);
    }

    @Test
    public void testMakeMoveLegalMoveWhiteWonByCheckmate() throws Exception {
        String newFen = "R3k3/8/4K3/8/8/8/8/8 b - - 1 1";

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        game.setFen("4k3/8/4K3/8/R7/8/8/8 w - - 0 1");
        game.setLegalMoves("a4a1 a4a2 a4a3 a4b4 a4c4 a4d4 a4e4 a4f4 a4g4 a4h4 a4a5 a4a6 a4a7 a4a8 e6d5 e6e5 e6f5 e6d6 e6f6 ");
        game.setPlayerToMove(white);
        Move previousMove = new Move(game, null, "f8e8",
                "4k3/8/4K3/8/R7/8/8/8 w - - 0 1"
        );
        game.setMoves(List.of(previousMove));
        Move move = new Move(game, previousMove, "a4a8", newFen);
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(game, previousMove, "a4a8", newFen)).thenReturn(move);

        mvc.perform(post("/app/move")
                .with(user("login0"))
                .param("game_id", "1")
                .param("square1", "a4")
                .param("square2", "a8")
                .param("promotion", "")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/app/game?id={id}&legal_move={legalMove}", "1", "true"));

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, times(1)).createNewMove(game, previousMove, "a4a8", newFen);
        verify(gamesRepository, times(1)).updateGame(
                game, newFen, "",
                true, Result.WHITE_WON_BY_CHECKMATE.getDescription(), move
        );
        verify(movesRepository, times(1)).updateMove(previousMove, move);
        verify(usersRepository, times(1)).updateRating(white, 10.0);
        verify(usersRepository, times(1)).updateRating(black, -10.0);
    }

    @Test
    public void testMakeMoveLegalMoveDrawByInsufficientMaterial() throws Exception {
        String newFen = "8/8/8/3k4/8/7K/8/8 b - - 0 1";

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        game.setFen("8/8/8/3k4/8/7p/7K/8 w - - 0 1");
        game.setLegalMoves("h2g1 h2h1 h2h3 h2g3 ");
        game.setPlayerToMove(white);
        Move previousMove = new Move(game, null, "c5d5",
                "8/8/8/3k4/8/7p/7K/8 w - - 0 1"
        );
        game.setMoves(List.of(previousMove));
        Move move = new Move(game, previousMove, "h2h3", newFen);
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(game, previousMove, "h2h3", newFen)).thenReturn(move);

        mvc.perform(post("/app/move")
                .with(user("login0"))
                .param("game_id", "1")
                .param("square1", "h2")
                .param("square2", "h3")
                .param("promotion", "")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/app/game?id={id}&legal_move={legalMove}", "1", "true"));

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, times(1)).createNewMove(game, previousMove, "h2h3", newFen);
        verify(gamesRepository, times(1)).updateGame(
                game, newFen, "d5c4 d5d4 d5e4 d5c5 d5e5 d5c6 d5d6 d5e6 ",
                true, Result.DRAW_BY_INSUFFICIENT_MATERIAL.getDescription(), move
        );
        verify(movesRepository, times(1)).updateMove(previousMove, move);
        verify(usersRepository, times(1)).updateRating(white, 0.0);
        verify(usersRepository, times(1)).updateRating(black, -0.0);
    }

    @Test
    public void testMakeMoveLegalMoveDrawByStalemate() throws Exception {
        String newFen = "4k3/4P3/4K3/8/8/8/8/8 b - - 1 1";

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        game.setFen("4k3/4P3/3K4/8/8/8/8/8 w - - 0 1");
        game.setLegalMoves("d6c5 d6d5 d6e5 d6c6 d6e6 d6c7 ");
        game.setPlayerToMove(white);
        Move previousMove = new Move(game, null, "c5d5",
                "4k3/4P3/3K4/8/8/8/8/8 w - - 0 1"
        );
        game.setMoves(List.of(previousMove));
        Move move = new Move(game, previousMove, "d8e8", newFen);
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(game, previousMove, "d6e6", newFen)).thenReturn(move);

        mvc.perform(post("/app/move")
                .with(user("login0"))
                .param("game_id", "1")
                .param("square1", "d6")
                .param("square2", "e6")
                .param("promotion", "")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/app/game?id={id}&legal_move={legalMove}", "1", "true"));

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, times(1)).createNewMove(game, previousMove, "d6e6", newFen);
        verify(gamesRepository, times(1)).updateGame(
                game, newFen, "",
                true, Result.DRAW_BY_STALEMATE.getDescription(), move
        );
        verify(movesRepository, times(1)).updateMove(previousMove, move);
        verify(usersRepository, times(1)).updateRating(white, 0.0);
        verify(usersRepository, times(1)).updateRating(black, -0.0);
    }

    @Test
    public void testMakeMoveLegalMoveDrawByFiftyMoveRule() throws Exception {
        String newFen = "8/8/8/3k4/8/7p/8/7K b - - 100 90";

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        game.setFen("8/8/8/3k4/8/7p/7K/8 w - - 99 90");
        game.setLegalMoves("h2g1 h2h1 h2h3 h2g3 ");
        game.setPlayerToMove(white);
        Move previousMove = new Move(game, null, "c5d5",
                "8/8/8/3k4/8/7p/7K/8 w - - 99 90"
        );
        game.setMoves(List.of(previousMove));
        Move move = new Move(game, previousMove, "h2h1", newFen);
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(game, previousMove, "h2h1", newFen)).thenReturn(move);

        mvc.perform(post("/app/move")
                .with(user("login0"))
                .param("game_id", "1")
                .param("square1", "h2")
                .param("square2", "h1")
                .param("promotion", "")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/app/game?id={id}&legal_move={legalMove}", "1", "true"));

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, times(1)).createNewMove(game, previousMove, "h2h1", newFen);
        verify(gamesRepository, times(1)).updateGame(
                game, newFen, "h3h2 d5c4 d5d4 d5e4 d5c5 d5e5 d5c6 d5d6 d5e6 ",
                true, Result.DRAW_BY_FIFTY_MOVE_RULE.getDescription(), move
        );
        verify(movesRepository, times(1)).updateMove(previousMove, move);
        verify(usersRepository, times(1)).updateRating(white, 0.0);
        verify(usersRepository, times(1)).updateRating(black, -0.0);
    }

    @Test
    public void testMakeMoveLegalMoveDrawByRepetition() throws Exception {
        String newFen = "rnbq1bnr/ppppkppp/8/4p3/4P3/8/PPPPKPPP/RNBQ1BNR w - - 10 7";

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        game.setFen("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPPKPPP/RNBQ1BNR b - - 9 6");
        game.setLegalMoves("a7a6 b7b6 c7c6 d7d6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 f7f5 g7g5 h7h5 b8a6 b8c6 g8f6 " +
                "g8h6 g8e7 f8a3 f8b4 f8c5 f8d6 f8e7 d8h4 d8g5 d8f6 d8e7 e8e7 ");
        game.setPlayerToMove(black);
        List<Move> moves = new ArrayList<>();
        AtomicInteger idCounter = new AtomicInteger(0);
        for (String fen : List.of(
                GameUtil.STARTING_POSITION_FEN,
                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
                "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
                "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPPKPPP/RNBQ1BNR b kq - 1 2",
                "rnbq1bnr/ppppkppp/8/4p3/4P3/8/PPPPKPPP/RNBQ1BNR w - - 2 3",
                "rnbq1bnr/ppppkppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR b - - 3 3",
                "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w - - 4 4",
                "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPPKPPP/RNBQ1BNR b - - 5 4",
                "rnbq1bnr/ppppkppp/8/4p3/4P3/8/PPPPKPPP/RNBQ1BNR w - - 6 5",
                "rnbq1bnr/ppppkppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR b - - 7 5",
                "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w - - 8 6"
        )) {
            Move m = new Move(game, null, "a1a1", "newfen");
            m.setRepetitionInfo(GameUtil.getPositionFromFen(fen));
            m.setId(idCounter.getAndIncrement());
            moves.add(m);
        }
        game.setMoves(moves);
        Move previousMove = moves.get(moves.size() - 1);
        previousMove.setFen("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPPKPPP/RNBQ1BNR b - - 9 6");
        Move move = new Move(game, previousMove, "e8e7", newFen);
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(game, previousMove, "e8e7", newFen)).thenReturn(move);

        mvc.perform(post("/app/move")
                .with(user("login0"))
                .param("game_id", "1")
                .param("square1", "e8")
                .param("square2", "e7")
                .param("promotion", "")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/app/game?id={id}&legal_move={legalMove}", "1", "true"));

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, times(1)).createNewMove(game, previousMove, "e8e7", newFen);
        verify(gamesRepository, times(1)).updateGame(
                game, newFen, "a2a3 b2b3 c2c3 d2d3 f2f3 g2g3 h2h3 a2a4 b2b4 c2c4 d2d4 f2f4 g2g4 h2h4 b1a3 " +
                        "b1c3 g1f3 g1h3 d1e1 e2e1 e2d3 e2e3 e2f3 ",
                true, Result.DRAW_BY_REPETITION.getDescription(), move
        );
        verify(movesRepository, times(1)).updateMove(previousMove, move);
        verify(usersRepository, times(1)).updateRating(white, 0.0);
        verify(usersRepository, times(1)).updateRating(black, -0.0);
    }

    @Test
    public void testMakeMoveIllegalMove() throws Exception {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        Move move = new Move(game, null, "a1a1", "qwerty");
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(eq(game), eq(null), eq("a1a1"), anyString())).thenReturn(move);

        mvc.perform(post("/app/move")
                .with(user("login0"))
                .param("game_id", "1")
                .param("square1", "a1")
                .param("square2", "a1")
                .param("promotion", "")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/app/game?id={id}&illegal_move={illegalMove}", "1", "true"));

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, never()).createNewMove(any(Game.class), eq(null), anyString(), anyString());
        verify(gamesRepository, never()).updateGame(
                any(Game.class), anyString(), anyString(), anyBoolean(), anyString(), any(Move.class)
        );
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testMakeMoveError() throws Exception {
        String newFen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";

        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        game.setMoves(Collections.emptyList());
        Move move = new Move(game, null, "e2e4", newFen);
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        when(movesRepository.createNewMove(game, null, "e2e4", newFen)).thenReturn(move);
        when(gamesRepository.updateGame(
                game, newFen,
                "a7a6 b7b6 c7c6 d7d6 e7e6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 e7e5 f7f5 g7g5 h7h5 b8a6 b8c6 g8f6 g8h6 ",
                false, null, move)
        ).thenThrow(RuntimeException.class);

        mvc.perform(post("/app/move")
                .with(user("login0"))
                .param("game_id", "1")
                .param("square1", "e2")
                .param("square2", "e4")
                .param("promotion", "")
                .with(csrf())
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlTemplate("/app/game?id={id}&error={error}", "1", "true"));

        verify(gamesRepository, times(1)).findById(1);
        verify(movesRepository, times(1)).createNewMove(game, null, "e2e4", newFen);
        verify(gamesRepository, times(1)).updateGame(
                game, newFen,
                "a7a6 b7b6 c7c6 d7d6 e7e6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 e7e5 f7f5 g7g5 h7h5 b8a6 b8c6 g8f6 g8h6 ",
                false, null, move
        );
        verifyNoInteractions(usersRepository);
    }

    @Test
    public void testMakeMoveInterruptedException() {
        User white = new User("login0", "pass0");
        User black = new User("login1", "pass1");
        Game game = new Game(white, black);
        game.setMoves(Collections.emptyList());
        when(gamesRepository.findById(1)).thenReturn(Optional.of(game));
        MockedStatic<StockfishUtil> mockedStockfish = mockStatic(StockfishUtil.class);
        mockedStockfish.when(() -> StockfishUtil.makeMove(anyString(), anyString())).thenThrow(InterruptedException.class);

        assertThrows(NestedServletException.class, () -> mvc.perform(post("/app/move")
                .with(user("login0"))
                .param("game_id", "1")
                .param("square1", "e2")
                .param("square2", "e4")
                .param("promotion", "")
                .with(csrf())
        ));

        mockedStockfish.close();
        verify(gamesRepository, times(1)).findById(1);
        verify(gamesRepository, never()).updateGame(
                eq(game), anyString(),
                eq("a7a6 b7b6 c7c6 d7d6 e7e6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 e7e5 f7f5 g7g5 h7h5 b8a6 b8c6 g8f6 g8h6 "),
                eq(false), eq(null), any(Move.class)
        );
        verifyNoInteractions(usersRepository, movesRepository);
    }
}