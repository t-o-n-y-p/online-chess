package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesDao;
import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
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
public class MainPageController {
    public static final int MAIN_PAGE_RESULTS_MOBILE = 5;
    public static final int MAIN_PAGE_RESULTS = 10;

    private final UsersDao usersDao;
    private final ChallengesDao challengesDao;
    private final GamesDao gamesDao;

    public MainPageController(UsersDao usersDao, ChallengesDao challengesDao, GamesDao gamesDao) {
        this.usersDao = usersDao;
        this.challengesDao = challengesDao;
        this.gamesDao = gamesDao;
    }

    @GetMapping("/main")
    public String main(RedirectAttributes attributes, Model model,
                       @RequestParam(defaultValue = "false", name = "challenge_created") boolean challengeCreated,
                       @RequestParam(defaultValue = "false", name = "challenge_accepted") boolean challengeAccepted,
                       @RequestParam(defaultValue = "false") boolean error,
                       @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return "redirect:login";
        }
        model.addAttribute("challengeCreated", challengeCreated);
        model.addAttribute("challengeAccepted", challengeAccepted);
        model.addAttribute("error", error);
        User user = usersDao.findByLogin(session.getLogin());
        model.addAttribute("user", user);
        List<Challenge> incomingChallenges = challengesDao.findIncomingChallenges(user, 0, MAIN_PAGE_RESULTS + 1);
        model.addAttribute("incomingChallenges",
                incomingChallenges.subList(0, Integer.min(incomingChallenges.size(), MAIN_PAGE_RESULTS)));
        model.addAttribute("incomingChallengesMobile",
                incomingChallenges.subList(0, Integer.min(incomingChallenges.size(), MAIN_PAGE_RESULTS_MOBILE)));
        model.addAttribute("canViewAllChallenges", incomingChallenges.size() > MAIN_PAGE_RESULTS);
        model.addAttribute("canViewAllChallengesMobile", incomingChallenges.size() > MAIN_PAGE_RESULTS_MOBILE);
        List<Game> games = gamesDao.findByUser(user, 0, MAIN_PAGE_RESULTS + 1);
        model.addAttribute("games", games.subList(0, Integer.min(games.size(), MAIN_PAGE_RESULTS)));
        model.addAttribute("gamesMobile", games.subList(0, Integer.min(games.size(), MAIN_PAGE_RESULTS_MOBILE)));
        model.addAttribute("canViewAllGames", games.size() > MAIN_PAGE_RESULTS);
        model.addAttribute("canViewAllGamesMobile", games.size() > MAIN_PAGE_RESULTS_MOBILE);
        return "_main";
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

}
