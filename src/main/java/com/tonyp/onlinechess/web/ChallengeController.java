package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.ChallengesRepository;
import com.tonyp.onlinechess.dao.GamesRepository;
import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.Challenge;
import com.tonyp.onlinechess.model.Color;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.web.services.ChallengeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.EntityManager;
import java.util.Optional;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Controller
@SessionAttributes("user-session")
@AllArgsConstructor
public class ChallengeController {

    private final UsersRepository usersRepository;
    private final ChallengesRepository challengesRepository;
    private final GamesRepository gamesRepository;
    private final ChallengeService challengeService;

    @PostMapping("/challenge/accept")
    public RedirectView accept(RedirectAttributes attributes,
                               @RequestParam int id,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "false", name = "to_previous_page") boolean toPreviousPage,
                               @RequestParam(defaultValue = "false", name = "from_challenges") boolean fromChallenges,
                               @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return new RedirectView("../login");
        }
        try {
            Challenge acceptedChallenge = challengesRepository.findById(id).get();
            challengeService.acceptChallenge(acceptedChallenge);

            attributes.addAttribute("challenge_accepted", true);
            return getAcceptChallengeRedirectView(attributes, page, toPreviousPage, fromChallenges);
        } catch (Throwable e) {
            attributes.addAttribute("error", true);
            return getAcceptChallengeRedirectView(attributes, page, toPreviousPage, fromChallenges);
        }
    }

    @PostMapping("/challenge")
    public RedirectView create(RedirectAttributes attributes,
                               @RequestParam(name = "opponent_id") int opponentId,
                               @RequestParam(name = "target_color") Color targetColor,
                               @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() == null) {
            attributes.addAttribute("force_logout", true);
            return new RedirectView("/login");
        }
        try {
            challengesRepository.createNewChallenge(
                    usersRepository.findByLogin(session.getLogin()),
                    usersRepository.findById(opponentId).get(),
                    targetColor
            );

            attributes.addAttribute("challenge_created", true);
            return new RedirectView("/main");
        } catch (Throwable e) {
            attributes.addAttribute("error", true);
            return new RedirectView("/main");
        }
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

    private RedirectView getAcceptChallengeRedirectView
            (RedirectAttributes attributes, int page, boolean toPreviousPage, boolean fromChallenges) {
        if (fromChallenges) {
            if (toPreviousPage) {
                attributes.addAttribute("page", page - 1);
            } else {
                attributes.addAttribute("page", page);
            }
            return new RedirectView("/challenges");
        } else {
            return new RedirectView("/main");
        }
    }
}
