package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.web.services.GameService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.NoSuchElementException;

@Controller
@AllArgsConstructor
public class ResignController {

    private final UsersRepository usersRepository;
    private final GamesRepository gamesRepository;
    private final GameService gameService;

    @PostMapping("/app/resign")
    public RedirectView resign(RedirectAttributes attributes,
                               @RequestParam(name = "game_id") int gameId,
                               Authentication authentication) {
        try {
            Game game = gamesRepository.findById(gameId).orElseThrow(NoSuchElementException::new);
            User user = usersRepository.findByLogin(authentication.getName());
            gameService.resign(game, user);

            attributes.addAttribute("id", gameId);
            attributes.addAttribute("resignation", true);
            return new RedirectView("/app/game");
        } catch (Throwable e) {
            attributes.addAttribute("id", gameId);
            attributes.addAttribute("error", true);
            return new RedirectView("/app/game");
        }
    }
}
