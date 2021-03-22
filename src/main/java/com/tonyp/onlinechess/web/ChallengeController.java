package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.web.services.ChallengeService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.NoSuchElementException;

@Controller
@AllArgsConstructor
public class ChallengeController {

    private final UsersRepository usersRepository;
    private final ChallengesRepository challengesRepository;
    private final ChallengeService challengeService;

    private static final String mainPageUrl = "/app/main";

    @PostMapping("/app/challenge/accept")
    public RedirectView accept(RedirectAttributes attributes,
                               @RequestParam int id,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "false", name = "to_previous_page") boolean toPreviousPage,
                               @RequestParam(defaultValue = "false", name = "from_challenges") boolean fromChallenges,
                               Authentication authentication) {
        try {
            Challenge acceptedChallenge = challengesRepository.findById(id).orElseThrow(NoSuchElementException::new);
            challengeService.acceptChallenge(acceptedChallenge);

            attributes.addAttribute("challenge_accepted", true);
            return getAcceptChallengeRedirectView(attributes, page, toPreviousPage, fromChallenges);
        } catch (Throwable e) {
            attributes.addAttribute("error", true);
            return getAcceptChallengeRedirectView(attributes, page, toPreviousPage, fromChallenges);
        }
    }

    @PostMapping("/app/challenge")
    public RedirectView create(RedirectAttributes attributes,
                               @RequestParam(name = "opponent_id") int opponentId,
                               @RequestParam(name = "target_color") Color targetColor,
                               Authentication authentication) {
        try {
            challengesRepository.createNewChallenge(
                    usersRepository.findByLogin(authentication.getName()),
                    usersRepository.findById(opponentId).orElseThrow(NoSuchElementException::new),
                    targetColor
            );

            attributes.addAttribute("challenge_created", true);
            return new RedirectView(mainPageUrl);
        } catch (Throwable e) {
            attributes.addAttribute("error", true);
            return new RedirectView(mainPageUrl);
        }
    }

    private RedirectView getAcceptChallengeRedirectView
            (RedirectAttributes attributes, int page, boolean toPreviousPage, boolean fromChallenges) {
        if (fromChallenges) {
            if (toPreviousPage) {
                attributes.addAttribute("page", page - 1);
            } else {
                attributes.addAttribute("page", page);
            }
            return new RedirectView("/app/challenges");
        } else {
            return new RedirectView(mainPageUrl);
        }
    }
}
