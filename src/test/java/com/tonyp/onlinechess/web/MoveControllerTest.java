package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.MovesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.GameUtil;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlTemplate;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    public void makeMove() throws Exception {

//        try (MockedStatic<StockfishUtil> stockfishUtilMockedStatic = Mockito.mockStatic(StockfishUtil.class)) {
//            stockfishUtilMockedStatic.when(() -> StockfishUtil.makeMove(eq(GameUtil.STARTING_POSITION_FEN), eq("e2e4")))
//                    .thenReturn("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
//            stockfishUtilMockedStatic.when(() -> StockfishUtil.getLegalMoves(eq("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1")))
//                    .thenReturn("a7a6 b7b6 c7c6 d7d6 e7e6 f7f6 g7g6 h7h6 a7a5 b7b5 c7c5 d7d5 e7e5 f7f5 g7g5 h7h5 b8a6 b8c6 g8f6 g8h6");
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

            verify(movesDao, times(1)).createNewMove(game, "e2e4");
//            stockfishUtilMockedStatic.verify(
//                    times(1),
//                    () -> StockfishUtil.makeMove(GameUtil.STARTING_POSITION_FEN, "e2e4")
//            );
//        }
    }
}