package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
@SessionAttributes("user-session")
public class ChallengePageController {

    public static final int PAGE_RESULTS = 8;
    public static final double RATING_THRESHOLD = 50.0;

    private final EntityManager manager;
    private final UsersDao usersDao;

    public ChallengePageController(EntityManager manager, UsersDao usersDao) {
        this.manager = manager;
        this.usersDao = usersDao;
    }

    @GetMapping("/challenge/step1")
    public String step1(RedirectAttributes attributes, Model model,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "") String search,
                        @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return "redirect:../login";
        }
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        User user = usersDao.findByLogin(session.getLogin());
        List<User> opponents = usersDao.findOpponentsByRatingAndLoginInput(
                    user, search, user.getRating(), RATING_THRESHOLD, (page - 1) * PAGE_RESULTS, PAGE_RESULTS + 1
        );
        model.addAttribute("opponents", opponents.subList(0, Integer.min(opponents.size(), PAGE_RESULTS)));
        model.addAttribute("nextPageAvailable", opponents.size() > PAGE_RESULTS);

        return "challenge/_step1";
    }

    @GetMapping("/challenge/step2")
    public String step2(RedirectAttributes attributes, Model model,
                        @RequestParam(name = "opponent_id") int opponentId,
                        @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return "redirect:../login";
        }
        User opponent = manager.find(User.class, opponentId);
        model.addAttribute("opponent", opponent);
        return "challenge/_step2";
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }
}
