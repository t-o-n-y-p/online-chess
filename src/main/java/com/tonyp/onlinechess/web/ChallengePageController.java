package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
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

@SuppressWarnings("OptionalGetWithoutIsPresent")
@Controller
@AllArgsConstructor
public class ChallengePageController {

    public static final int PAGE_RESULTS = 8;
    public static final double RATING_THRESHOLD = 50.0;

    private final UsersRepository usersRepository;

    @GetMapping("/app/challenge/step1")
    public String step1(Model model,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "") String search,
                        Authentication authentication) {
        model.addAttribute("search", search);
        model.addAttribute("page", page);
        User user = usersRepository.findByLogin(authentication.getName());
        Page<User> opponents = usersRepository.findOpponentsByRatingAndLoginInput(
                    user, search,
                user.getRating() - RATING_THRESHOLD,
                user.getRating() + RATING_THRESHOLD, PageRequest.of(page - 1, PAGE_RESULTS)
        );
        model.addAttribute("opponents", opponents);

        return "challenge/_step1";
    }

    @GetMapping("/app/challenge/step2")
    public String step2(Model model,
                        @RequestParam(name = "opponent_id") int opponentId,
                        Authentication authentication) {
        User opponent = usersRepository.findById(opponentId).get();
        model.addAttribute("opponent", opponent);
        return "challenge/_step2";
    }
}
