package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@SessionAttributes("user-session")
@AllArgsConstructor
public class GameListPageController {

    public static final int PAGE_RESULTS = 8;

    private final UsersRepository usersRepository;
    private final GamesRepository gamesRepository;

    @GetMapping("/games")
    public String games(RedirectAttributes attributes, Model model,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "") String search,
                        @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return "redirect:login";
        }
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        User user = usersRepository.findByLogin(session.getLogin());
        model.addAttribute("user", user);
        Page<Game> games = gamesRepository.findByUserAndOpponentLoginInput(
                user, search, PageRequest.of(page - 1, PAGE_RESULTS)
        );
        model.addAttribute("games", games);

        return "_games";
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

}
