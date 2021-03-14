package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.web.services.GameService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Controller
@SessionAttributes("user-session")
@AllArgsConstructor
public class MoveController {

    private final GamesRepository gamesRepository;
    private final GameService gameService;

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
            Game game = gamesRepository.findById(gameId).get();
            String notation = square1 + square2 + promotion;
            if (!game.getLegalMoves().contains(notation)) {
                attributes.addAttribute("id", gameId);
                attributes.addAttribute("illegal_move", true);
                return new RedirectView("/game");
            }
            gameService.makeMove(game, notation);

            attributes.addAttribute("id", gameId);
            attributes.addAttribute("legal_move", true);
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
