package com.tonyp.onlinechess.web;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
public class LoginController {

    @GetMapping("/login")
    public String getLogin(Authentication authentication) {
        if (authentication != null) {
            return "redirect:/app/main";
        }
        return "_login";
    }

}
