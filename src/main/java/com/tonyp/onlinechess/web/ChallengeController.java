package com.tonyp.onlinechess.web;

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
        if (fromChallenges) {
            if (toPreviousPage) {
                attributes.addAttribute("page", page - 1);
            } else {
                attributes.addAttribute("page", page);
            }
            attributes.addAttribute("challenge_accepted", true);
            return new RedirectView("/challenges");
        } else {
            attributes.addAttribute("challenge_accepted", true);
            return new RedirectView("/main");
        }
    }

}
