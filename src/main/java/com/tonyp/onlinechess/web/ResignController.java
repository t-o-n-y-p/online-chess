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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.EntityManager;

@Controller
@SessionAttributes("user-session")
public class ResignController {

    private EntityManager manager;
    private UsersDao usersDao;
    private GamesDao gamesDao;

    public ResignController(EntityManager manager, UsersDao usersDao, GamesDao gamesDao) {
        this.manager = manager;
        this.usersDao = usersDao;
        this.gamesDao = gamesDao;
    }

    @PostMapping("/resign")
    public RedirectView resign(RedirectAttributes attributes, @RequestParam(name = "game_id") int gameId,
                               @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return new RedirectView("/login");
        }
        try {
            Game game = manager.find(Game.class, gameId);
            User user = usersDao.findByLogin(session.getLogin());
            Result currentResult = user.equals(game.getWhite())
                    ? Result.BLACK_WON_BY_RESIGNATION
                    : Result.WHITE_WON_BY_RESIGNATION;

            manager.getTransaction().begin();
            gamesDao.updateGame(game, true, currentResult.getDescription());
            double ratingDifference = GameUtil.getRatingDifference(game.getWhite(), game.getBlack(), currentResult);
            usersDao.updateRating(game.getWhite(), ratingDifference);
            usersDao.updateRating(game.getBlack(), -ratingDifference);
            manager.getTransaction().commit();

            attributes.addAttribute("id", gameId);
            attributes.addAttribute("resignation", true);
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
