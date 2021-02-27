package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesDao;
import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import java.util.List;

@Controller
public class MainPageController {
    private final int MAIN_PAGE_RESULTS = 10;

    @Autowired
    private UsersDao usersDao;

    @Autowired
    private ChallengesDao challengesDao;

    @Autowired
    private GamesDao gamesDao;

    @GetMapping("/main")
    public String main(Model model,
                       @RequestParam(defaultValue = "false", name = "challenge_created") boolean challengeCreated,
                       @RequestParam(defaultValue = "false", name = "challenge_accepted") boolean challengeAccepted,
                       @RequestParam(defaultValue = "false") boolean error) {
        model.addAttribute("challengeCreated", challengeCreated);
        model.addAttribute("challengeAccepted", challengeAccepted);
        model.addAttribute("error", error);
        User user = usersDao.findByLogin(OnlineChessApplication.USER_LOGIN);
        model.addAttribute("user", user);
        List<Challenge> incomingChallenges = challengesDao.findIncomingChallenges(user, 0, MAIN_PAGE_RESULTS + 1);
        model.addAttribute("incomingChallenges",
                incomingChallenges.subList(0, Integer.min(incomingChallenges.size(), MAIN_PAGE_RESULTS)));
        model.addAttribute("canViewAllChallenges", incomingChallenges.size() > MAIN_PAGE_RESULTS);
        List<Game> games = gamesDao.findByUser(user, 0, MAIN_PAGE_RESULTS + 1);
        model.addAttribute("games", games.subList(0, Integer.min(games.size(), MAIN_PAGE_RESULTS)));
        model.addAttribute("canViewAllGames", games.size() > MAIN_PAGE_RESULTS);
        return "main";
    }

}
