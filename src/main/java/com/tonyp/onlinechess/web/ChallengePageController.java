package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
@SessionAttributes("user-session")
public class ChallengePageController {

    private final int COLUMNS = 2;
    private final int PAGE_RESULTS = 20;
    private final int RATING_THRESHOLD = 50;

    private final EntityManager manager;
    private final UsersDao usersDao;

    public ChallengePageController(@Autowired EntityManager manager, @Autowired UsersDao usersDao) {
        this.manager = manager;
        this.usersDao = usersDao;
    }

    @GetMapping("/challenge/step1")
    public String step1(Model model,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "") String search,
                        @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            return "redirect:login";
        }
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        model.addAttribute("columns", COLUMNS);
        User user = usersDao.findByLogin(session.getLogin());
        List<User> opponents = usersDao.findOpponentsByRatingAndLoginInput(
                    user, search, user.getRating(), RATING_THRESHOLD, (page - 1) * PAGE_RESULTS, PAGE_RESULTS + 1
        );
        AtomicInteger counter = new AtomicInteger(0);
        Map<Integer, List<User>> opponentsMap = opponents.stream()
                .limit(PAGE_RESULTS)
                .collect(Collectors.groupingBy(i -> counter.getAndIncrement() % COLUMNS));
        model.addAttribute("opponentsMap", opponentsMap);
        model.addAttribute("nextPageAvailable", opponents.size() > PAGE_RESULTS);

        return "challenge/step1";
    }

    @GetMapping("/challenge/step2")
    public String step2(Model model,
                        @RequestParam(name = "opponent_id") int opponentId,
                        @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            return "redirect:login";
        }
        User opponent = manager.find(User.class, opponentId);
        model.addAttribute("opponent", opponent);
        return "challenge/step2";
    }
}
