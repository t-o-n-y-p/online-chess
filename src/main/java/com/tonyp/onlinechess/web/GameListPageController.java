package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
@SessionAttributes("user-session")
public class GameListPageController {

    public static final int PAGE_RESULTS = 8;

    private final UsersDao usersDao;
    private final GamesDao gamesDao;

    public GameListPageController(@Autowired UsersDao usersDao, @Autowired GamesDao gamesDao) {
        this.usersDao = usersDao;
        this.gamesDao = gamesDao;
    }

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
        User user = usersDao.findByLogin(session.getLogin());
        model.addAttribute("user", user);
        List<Game> games = gamesDao.findByUserAndOpponentLoginInput(
                user, search, (page - 1) * PAGE_RESULTS, PAGE_RESULTS + 1
        );
        model.addAttribute("games", games.subList(0, Integer.min(games.size(), PAGE_RESULTS)));
        model.addAttribute("nextPageAvailable", games.size() > PAGE_RESULTS);

        return "_games";
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

}
