package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Game;
import com.tonyp.onlinechess.model.User;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class GameListPageController {

    public static final int PAGE_RESULTS = 8;

    private final UsersRepository usersRepository;
    private final GamesRepository gamesRepository;

    @GetMapping("/app/games")
    public String games(Model model,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "") String search,
                        Authentication authentication) {
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        User user = usersRepository.findByLogin(authentication.getName());
        model.addAttribute("user", user);
        Page<Game> games = gamesRepository.findByUserAndOpponentLoginInput(
                user, search, PageRequest.of(page - 1, PAGE_RESULTS)
        );
        model.addAttribute("games", games);

        return "_games";
    }

}
