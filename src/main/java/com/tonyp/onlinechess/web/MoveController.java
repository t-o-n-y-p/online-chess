package com.tonyp.onlinechess.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class MoveController {

    @PostMapping("/move")
    public RedirectView makeMove(RedirectAttributes attributes, @RequestParam(name = "game_id") int gameId,
                                 @RequestParam String square1,
                                 @RequestParam String square2,
                                 @RequestParam String promotion) {
        System.out.println(gameId);
        System.out.println(square1 + square2 + promotion);
        attributes.addAttribute("id", gameId);
        attributes.addAttribute("legal_move", true);
        return new RedirectView("/game");
    }

}