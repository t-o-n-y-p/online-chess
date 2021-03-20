package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.GameUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Controller
@AllArgsConstructor
public class GamePageController {

    private final UsersRepository usersRepository;
    private final GamesRepository gamesRepository;

    @GetMapping("/app/game")
    public String game(Model model,
                       @RequestParam int id,
                       @RequestParam(defaultValue = "false", name = "legal_move") boolean legalMove,
                       @RequestParam(defaultValue = "false", name = "illegal_move") boolean illegalMove,
                       @RequestParam(defaultValue = "false") boolean resignation,
                       @RequestParam(defaultValue = "false") boolean error,
                       Authentication authentication) {
        model.addAttribute("legalMove", legalMove);
        model.addAttribute("illegalMove", illegalMove);
        model.addAttribute("resignation", resignation);
        model.addAttribute("error", error);
        User user = usersRepository.findByLogin(authentication.getName());
        Game game = gamesRepository.findById(id).get();
        model.addAttribute("user", user);
        model.addAttribute("game", game);
        model.addAttribute("board", GameUtil.getBoard(
                game.getFen(), game.getWhite().equals(user) ? Color.WHITE : Color.BLACK
        ));
        model.addAttribute("squares", GameUtil.SQUARES);
        return "_game";
    }
}
