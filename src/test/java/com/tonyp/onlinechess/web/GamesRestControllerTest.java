package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.MovesRepository;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.IdOnlyRestView;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.LastMoveRestView;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.GameUtil;
import com.tonyp.onlinechess.tools.StockfishUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.tonyp.onlinechess.web.AppJpaConfiguration.JSON_DATE_FORMAT;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = TestWebConfiguration.class)
public class GamesRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MovesRepository movesRepository;

    @Test
    public void testFindLastMoveNotAuthorized() throws Exception {
        mvc.perform(get("/api/game/1/lastMove"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testFindLastMoveSuccess() throws Exception {
        User white = new User("login0", "password0");
        white.setId(1);
        User black = new User("login1", "password1");
        black.setId(2);
        Game game = new Game(white, black);
        game.setId(3);
        Move previousMove = new Move(game, null, "e2e4", game.getFen());
        previousMove.setId(4);
        String fen = StockfishUtil.makeMove(GameUtil.STARTING_POSITION_FEN, "e2e4");
        game.setFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
        Move move = new Move(game, previousMove, "e7e5", fen);
        move.setId(5);
        when(movesRepository.findFirstByGame_IdOrderByIdDesc(3, LastMoveRestView.class)).thenReturn(new LastMoveRestView() {
            @Override
            public int getId() {
                return 5;
            }

            @Override
            public Game getGame() {
                return game;
            }

            @Override
            public IdOnlyRestView getPreviousMove() {
                return () -> 4;
            }

            @Override
            public IdOnlyRestView getNextMove() {
                return null;
            }

            @Override
            public String getNotation() {
                return move.getNotation();
            }

            @Override
            public String getRepetitionInfo() {
                return move.getRepetitionInfo();
            }

            @Override
            public String getBoard() {
                return move.getBoard();
            }

            @Override
            public UUID getUuid() {
                return move.getUuid();
            }
        });

        mvc.perform(get("/api/game/3/lastMove")
                .with(user("login0"))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.uuid").value(move.getUuid().toString()))
                .andExpect(jsonPath("$.board").value(move.getBoard()))
                .andExpect(jsonPath("$.repetitionInfo").value(move.getRepetitionInfo()))
                .andExpect(jsonPath("$.notation").value("1. ... e7e5"))
                .andExpect(jsonPath("$.nextMove").isEmpty())
                .andExpect(jsonPath("$.previousMove.id").value(4))
                .andExpect(jsonPath("$.game.id").value(3))
                .andExpect(jsonPath("$.game.completed").value(false))
                .andExpect(jsonPath("$.game.description").isEmpty())
                .andExpect(jsonPath("$.game.legalMoves").value(game.getLegalMoves()))
                .andExpect(jsonPath("$.game.lastModifiedTimestamp")
                        .value(game.getLastModifiedTimestamp().format(DateTimeFormatter.ofPattern(JSON_DATE_FORMAT))))
                .andExpect(jsonPath("$.game.uuid").value(game.getUuid().toString()))
                .andExpect(jsonPath("$.game.fen").value(game.getFen()))
                .andExpect(jsonPath("$.game.white.id").value(1))
                .andExpect(jsonPath("$.game.white.login").value("login0"))
                .andExpect(jsonPath("$.game.white.rating").value(1200.0))
                .andExpect(jsonPath("$.game.black.id").value(2))
                .andExpect(jsonPath("$.game.black.login").value("login1"))
                .andExpect(jsonPath("$.game.black.rating").value(1200.0))
                .andExpect(jsonPath("$.game.playerToMove.id").value(1))
                .andExpect(jsonPath("$.game.playerToMove.login").value("login0"))
                .andExpect(jsonPath("$.game.playerToMove.rating").value(1200.0));
    }

    @Test
    public void testFindLastMoveNoMove() throws Exception {
        when(movesRepository.findFirstByGame_IdOrderByIdDesc(1, LastMoveRestView.class)).thenReturn(null);

        mvc.perform(get("/api/game/1/lastMove")
                .with(user("login0"))
        )
                .andExpect(status().isNoContent());
    }

    @Test
    public void testFindLastMoveError() throws Exception {
        when(movesRepository.findFirstByGame_IdOrderByIdDesc(1, LastMoveRestView.class)).thenThrow(RuntimeException.class);

        mvc.perform(get("/api/game/1/lastMove")
                .with(user("login0"))
        )
                .andExpect(status().isInternalServerError());
    }

}