package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesDao;
import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.Challenge;
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
public class ChallengeListPageController {

    public static final int PAGE_RESULTS = 8;

    private final UsersDao usersDao;
    private final ChallengesDao challengesDao;

    public ChallengeListPageController(@Autowired UsersDao usersDao, @Autowired ChallengesDao challengesDao) {
        this.usersDao = usersDao;
        this.challengesDao = challengesDao;
    }

    @GetMapping("/challenges")
    public String challenges(RedirectAttributes attributes, Model model,
                             @RequestParam(defaultValue = "false", name = "challenge_accepted") boolean challengeAccepted,
                             @RequestParam(defaultValue = "false") boolean error,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "") String search,
                             @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return "redirect:login";
        }
        model.addAttribute("challengeAccepted", challengeAccepted);
        model.addAttribute("error", error);
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        User user = usersDao.findByLogin(session.getLogin());
        model.addAttribute("user", user);
        List<Challenge> challenges = challengesDao.findIncomingChallengesByOpponentLoginInput(
                user, search, (page - 1) * PAGE_RESULTS, PAGE_RESULTS + 1
        );
        model.addAttribute("challenges", challenges.subList(0, Integer.min(challenges.size(), PAGE_RESULTS)));
        model.addAttribute("nextPageAvailable", challenges.size() > PAGE_RESULTS);
        model.addAttribute("toPreviousPage", challenges.size() == 1 && page > 1);

        return "_challenges";
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }
}
