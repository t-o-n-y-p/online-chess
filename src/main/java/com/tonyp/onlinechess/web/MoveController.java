package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.tools.GameUtil;
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
public class MoveController {

    private final GamesRepository gamesRepository;
    private final GameService gameService;

    @PostMapping("/app/move")
    public RedirectView makeMove(RedirectAttributes attributes, @RequestParam(name = "game_id") int gameId,
                                 @RequestParam String square1,
                                 @RequestParam String square2,
                                 @RequestParam String promotion,
                                 Authentication authentication) {
        String gamePageUrl = "/app/game";
        try {
            Game game = gamesRepository.findById(gameId).orElseThrow(NoSuchElementException::new);
            String notation = square1 + square2 + promotion;
            if (GameUtil.isIllegalMove(game, notation)) {
                attributes.addAttribute("id", gameId);
                attributes.addAttribute("illegal_move", true);
                return new RedirectView(gamePageUrl);
            }
            gameService.makeMove(game, notation);

            attributes.addAttribute("id", gameId);
            attributes.addAttribute("legal_move", true);
            return new RedirectView(gamePageUrl);
        } catch (Throwable e) {
            attributes.addAttribute("id", gameId);
            attributes.addAttribute("error", true);
            return new RedirectView(gamePageUrl);
        }
    }

}
