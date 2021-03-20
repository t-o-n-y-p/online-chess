package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Challenge;
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
import org.springframework.web.servlet.view.RedirectView;

@Controller
@AllArgsConstructor
public class MainPageController {
    public static final int MAIN_PAGE_RESULTS_MOBILE = 5;
    public static final int MAIN_PAGE_RESULTS = 8;

    private final UsersRepository usersRepository;
    private final ChallengesRepository challengesRepository;
    private final GamesRepository gamesRepository;

    @GetMapping({"/", "/app"})
    public RedirectView index() {
        return new RedirectView("app/main");
    }

    @GetMapping("/app/main")
    public String main(Model model,
                       @RequestParam(defaultValue = "false", name = "challenge_created") boolean challengeCreated,
                       @RequestParam(defaultValue = "false", name = "challenge_accepted") boolean challengeAccepted,
                       @RequestParam(defaultValue = "false") boolean error,
                       Authentication authentication) {
        model.addAttribute("challengeCreated", challengeCreated);
        model.addAttribute("challengeAccepted", challengeAccepted);
        model.addAttribute("error", error);
        User user = usersRepository.findByLogin(authentication.getName());
        model.addAttribute("user", user);
        Page<Challenge> incomingChallenges = challengesRepository.findByToOrderByTimestampDesc(
                user, PageRequest.of(0, MAIN_PAGE_RESULTS)
        );
        Page<Challenge> incomingChallengesMobile = challengesRepository.findByToOrderByTimestampDesc(
                user, PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE)
        );
        model.addAttribute("incomingChallenges", incomingChallenges);
        model.addAttribute("incomingChallengesMobile", incomingChallengesMobile);
        Page<Game> games = gamesRepository.findByUser(user, PageRequest.of(0, MAIN_PAGE_RESULTS));
        Page<Game> gamesMobile = gamesRepository.findByUser(user, PageRequest.of(0, MAIN_PAGE_RESULTS_MOBILE));
        model.addAttribute("games", games);
        model.addAttribute("gamesMobile", gamesMobile);
        return "_main";
    }
}
