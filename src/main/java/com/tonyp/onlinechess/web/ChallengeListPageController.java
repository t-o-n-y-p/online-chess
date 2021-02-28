package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesDao;
import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
public class ChallengeListPageController {

    private final int COLUMNS = 2;
    private final int PAGE_RESULTS = 20;

    private final UsersDao usersDao;
    private final ChallengesDao challengesDao;

    public ChallengeListPageController(@Autowired UsersDao usersDao, @Autowired ChallengesDao challengesDao) {
        this.usersDao = usersDao;
        this.challengesDao = challengesDao;
    }

    @GetMapping("/challenges")
    public String challenges(Model model,
                             @RequestParam(defaultValue = "false", name = "challenge_accepted") boolean challengeAccepted,
                             @RequestParam(defaultValue = "false") boolean error,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "") String search) {
        model.addAttribute("challengeAccepted", challengeAccepted);
        model.addAttribute("error", error);
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        model.addAttribute("columns", COLUMNS);
        User user = usersDao.findByLogin(OnlineChessApplication.USER_LOGIN);
        model.addAttribute("user", user);
        List<Challenge> challenges = challengesDao.findIncomingChallengesByOpponentLoginInput(
                user, search, (page - 1) * PAGE_RESULTS, PAGE_RESULTS + 1
        );
        AtomicInteger counter = new AtomicInteger(0);
        Map<Integer, List<Challenge>> challengesMap = challenges.stream()
                .limit(PAGE_RESULTS)
                .collect(Collectors.groupingBy(i -> counter.getAndIncrement() % COLUMNS));
        model.addAttribute("challengesMap", challengesMap);
        model.addAttribute("nextPageAvailable", challenges.size() > PAGE_RESULTS);
        model.addAttribute("toPreviousPage", challenges.size() == 1 && page > 1);

        return "challenges";
    }


}
