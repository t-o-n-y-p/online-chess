package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.tools.GameUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Controller
@SessionAttributes("user-session")
@AllArgsConstructor
public class GamePageController {

    private final UsersRepository usersRepository;
    private final GamesRepository gamesRepository;

    @GetMapping("/game")
    public String game(RedirectAttributes attributes, Model model,
                       @RequestParam int id,
                       @RequestParam(defaultValue = "false", name = "legal_move") boolean legalMove,
                       @RequestParam(defaultValue = "false", name = "illegal_move") boolean illegalMove,
                       @RequestParam(defaultValue = "false") boolean resignation,
                       @RequestParam(defaultValue = "false") boolean error,
                       @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return "redirect:login";
        }
        model.addAttribute("legalMove", legalMove);
        model.addAttribute("illegalMove", illegalMove);
        model.addAttribute("resignation", resignation);
        model.addAttribute("error", error);
        User user = usersRepository.findByLogin(session.getLogin());
        Game game = gamesRepository.findById(id).get();
        model.addAttribute("user", user);
        model.addAttribute("game", game);
        model.addAttribute("board", GameUtil.getBoard(
                game.getFen(), game.getWhite().equals(user) ? Color.WHITE : Color.BLACK
        ));
        model.addAttribute("squares", GameUtil.SQUARES);
        return "_game";
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

}
