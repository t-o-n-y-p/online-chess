package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.MovesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.Move;
import com.tonyp.onlinechess.tools.GameUtil;
import com.tonyp.onlinechess.tools.Result;
import com.tonyp.onlinechess.tools.StockfishUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Controller
@SessionAttributes("user-session")
public class MoveController {

    private EntityManager manager;
    private UsersDao usersDao;
    private GamesDao gamesDao;
    private MovesDao movesDao;

    public MoveController(EntityManager manager, UsersDao usersDao, GamesDao gamesDao, MovesDao movesDao) {
        this.manager = manager;
        this.usersDao = usersDao;
        this.gamesDao = gamesDao;
        this.movesDao = movesDao;
    }

    @PostMapping("/move")
    public RedirectView makeMove(RedirectAttributes attributes, @RequestParam(name = "game_id") int gameId,
                                 @RequestParam String square1,
                                 @RequestParam String square2,
                                 @RequestParam String promotion,
                                 @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return new RedirectView("/login");
        }
        try {
            Game game = manager.find(Game.class, gameId);
            String notation = square1 + square2 + promotion;
            if (!game.getLegalMoves().contains(notation)) {
                attributes.addAttribute("id", gameId);
                attributes.addAttribute("illegal_move", true);
                return new RedirectView("/game");
            }
            String newFen = StockfishUtil.makeMove(game.getFen(), notation);
            String newLegalMoves = StockfishUtil.getLegalMoves(newFen);
            boolean isCompleted = false;
            String description = null;
            Result currentResult = GameUtil.getResult(newFen, newLegalMoves, game.getMoves());
            if (currentResult != Result.UNDEFINED) {
                isCompleted = true;
                description = currentResult.getDescription();
            }

            manager.getTransaction().begin();
            Move newMove = movesDao.createNewMove(game, notation);
            gamesDao.updateGame(game, newFen, newLegalMoves, isCompleted, description, newMove);
            if (currentResult != Result.UNDEFINED) {
                double ratingDifference = GameUtil.getRatingDifference(game.getWhite(), game.getBlack(), currentResult);
                usersDao.updateRating(game.getWhite(), ratingDifference);
                usersDao.updateRating(game.getBlack(), -ratingDifference);
            }
            manager.getTransaction().commit();

            attributes.addAttribute("id", gameId);
            attributes.addAttribute("legal_move", true);
            return new RedirectView("/game");
        } catch (Throwable e) {
            attributes.addAttribute("id", gameId);
            attributes.addAttribute("error", true);
            return new RedirectView("/game");
        } finally {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
        }
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

}
