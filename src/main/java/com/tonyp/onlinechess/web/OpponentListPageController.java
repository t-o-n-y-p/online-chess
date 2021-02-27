package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
public class OpponentListPageController {

    private final int PAGE_RESULTS = 20;
    private final int RATING_THRESHOLD = 50;

    private final UsersDao usersDao;

    public OpponentListPageController(@Autowired UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    @GetMapping("/opponents")
    public String opponents(Model model,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "") String search) {
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        User user = usersDao.findByLogin(OnlineChessApplication.USER_LOGIN);
        List<User> opponents = usersDao.findOpponentsByRatingAndLoginInput(
                    user, search, user.getRating(), RATING_THRESHOLD, (page - 1) * PAGE_RESULTS, PAGE_RESULTS + 1
        );
        AtomicInteger counter = new AtomicInteger(0);
        Map<Integer, List<User>> opponentsMap = opponents.stream()
                .limit(PAGE_RESULTS)
                .collect(Collectors.groupingBy(i -> counter.getAndIncrement() % 2));
        model.addAttribute("opponentsMap", opponentsMap);
        model.addAttribute("nextPageAvailable", opponents.size() > PAGE_RESULTS);

        return "opponents";
    }
}
