package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.model.Color;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class ChallengeController {

    @PostMapping("/challenge/accept")
    public RedirectView accept(RedirectAttributes attributes, @RequestParam int id,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "false", name = "to_previous_page") boolean toPreviousPage,
                               @RequestParam(defaultValue = "false", name = "from_challenges") boolean fromChallenges) {
        attributes.addAttribute("challenge_accepted", true);
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

    @PostMapping("/challenge")
    public RedirectView challenge(RedirectAttributes attributes,
                                  @RequestParam(name = "opponent_id") int opponentId,
                                  @RequestParam(name = "target_color") Color targetColor) {
        attributes.addAttribute("challenge_created", true);
        return new RedirectView("/main");
    }
}