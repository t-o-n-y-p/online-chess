package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
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

import javax.persistence.EntityManager;
import java.util.List;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Controller
@SessionAttributes("user-session")
@AllArgsConstructor
public class ChallengePageController {

    public static final int PAGE_RESULTS = 8;
    public static final double RATING_THRESHOLD = 50.0;

    private final UsersRepository usersRepository;

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
        User user = usersRepository.findByLogin(session.getLogin());
        Page<User> opponents = usersRepository.findOpponentsByRatingAndLoginInput(
                    user, search,
                user.getRating() - RATING_THRESHOLD,
                user.getRating() + RATING_THRESHOLD, PageRequest.of(page - 1, PAGE_RESULTS)
        );
        model.addAttribute("opponents", opponents);

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
        User opponent = usersRepository.findById(opponentId).get();
        model.addAttribute("opponent", opponent);
        return "challenge/_step2";
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }
}
