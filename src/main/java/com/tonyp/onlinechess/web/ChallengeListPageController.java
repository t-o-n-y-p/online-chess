package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.User;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
public class ChallengeListPageController {

    public static final int PAGE_RESULTS = 8;

    private final UsersRepository usersRepository;
    private final ChallengesRepository challengesRepository;

    @GetMapping("/app/challenges")
    public String challenges(Model model,
                             @RequestParam(defaultValue = "false", name = "challenge_accepted") boolean challengeAccepted,
                             @RequestParam(defaultValue = "false") boolean error,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "") String search,
                             Authentication authentication) {
        model.addAttribute("challengeAccepted", challengeAccepted);
        model.addAttribute("error", error);
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        User user = usersRepository.findByLogin(authentication.getName());
        model.addAttribute("user", user);
        Page<Challenge> challenges = challengesRepository.findIncomingChallengesByOpponentLoginInput(
                user, search, PageRequest.of(page - 1, PAGE_RESULTS)
        );
        model.addAttribute("challenges", challenges);

        return "_challenges";
    }
}
