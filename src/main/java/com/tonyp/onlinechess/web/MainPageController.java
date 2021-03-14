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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes("user-session")
@AllArgsConstructor
public class MainPageController {
    public static final int MAIN_PAGE_RESULTS_MOBILE = 5;
    public static final int MAIN_PAGE_RESULTS = 8;

    private final UsersRepository usersRepository;
    private final ChallengesRepository challengesRepository;
    private final GamesRepository gamesRepository;

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
        User user = usersRepository.findByLogin(session.getLogin());
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

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

}
