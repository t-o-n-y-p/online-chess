package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesDao;
import com.tonyp.onlinechess.dao.UsersDao;
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
public class GameListPageController {

    private final int COLUMNS = 2;
    private final int PAGE_RESULTS = 20;

    private final UsersDao usersDao;
    private final GamesDao gamesDao;

    public GameListPageController(@Autowired UsersDao usersDao, @Autowired GamesDao gamesDao) {
        this.usersDao = usersDao;
        this.gamesDao = gamesDao;
    }

    @GetMapping("/games")
    public String opponents(Model model,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "") String search) {
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        model.addAttribute("columns", COLUMNS);
        User user = usersDao.findByLogin(OnlineChessApplication.USER_LOGIN);
        model.addAttribute("user", user);
        List<Game> games = gamesDao.findByUserAndOpponentLoginInput(
                user, search, (page - 1) * PAGE_RESULTS, PAGE_RESULTS + 1
        );
        AtomicInteger counter = new AtomicInteger(0);
        Map<Integer, List<Game>> gamesMap = games.stream()
                .limit(PAGE_RESULTS)
                .collect(Collectors.groupingBy(i -> counter.getAndIncrement() % COLUMNS));
        model.addAttribute("gamesMap", gamesMap);
        model.addAttribute("nextPageAvailable", games.size() > PAGE_RESULTS);

        return "games";
    }

}
