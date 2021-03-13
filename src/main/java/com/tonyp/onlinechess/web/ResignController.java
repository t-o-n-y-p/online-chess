package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.GameUtil;
import com.tonyp.onlinechess.tools.Result;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.EntityManager;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Controller
@SessionAttributes("user-session")
public class ResignController {

    private final UsersRepository usersRepository;
    private final GamesRepository gamesRepository;

    public ResignController(UsersRepository usersRepository, GamesRepository gamesRepository) {
        this.usersRepository = usersRepository;
        this.gamesRepository = gamesRepository;
    }

    @PostMapping("/resign")
    @Transactional
    public RedirectView resign(RedirectAttributes attributes, @RequestParam(name = "game_id") int gameId,
                               @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return new RedirectView("/login");
        }
        try {
            Game game = gamesRepository.findById(gameId).get();
            User user = usersRepository.findByLogin(session.getLogin());
            Result currentResult = user.equals(game.getWhite())
                    ? Result.BLACK_WON_BY_RESIGNATION
                    : Result.WHITE_WON_BY_RESIGNATION;

            gamesRepository.updateGame(game, true, currentResult.getDescription());
            double ratingDifference = GameUtil.getRatingDifference(game.getWhite(), game.getBlack(), currentResult);
            usersRepository.updateRating(game.getWhite(), ratingDifference);
            usersRepository.updateRating(game.getBlack(), -ratingDifference);

            attributes.addAttribute("id", gameId);
            attributes.addAttribute("resignation", true);
            return new RedirectView("/game");
        } catch (Throwable e) {
            attributes.addAttribute("id", gameId);
            attributes.addAttribute("error", true);
            return new RedirectView("/game");
        }
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

}
