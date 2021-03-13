package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.persistence.EntityManager;

@Controller
@SessionAttributes("user-session")
public class SignupController {

    private final UsersRepository usersRepository;

    public SignupController(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @GetMapping("/signup")
    public String signup(Model model,
                         @RequestParam(defaultValue = "false") boolean error,
                         @RequestParam(defaultValue = "false", name = "incorrect_login") boolean incorrectLogin,
                         @RequestParam(defaultValue = "false", name = "invalid_login") boolean invalidLogin,
                         @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() != null) {
            return "redirect:main";
        }
        model.addAttribute("error", error);
        model.addAttribute("incorrectLogin", incorrectLogin);
        model.addAttribute("invalidLogin", invalidLogin);
        return "_signup";
    }

    @PostMapping("/signup")
    @Transactional
    public RedirectView signup(RedirectAttributes attributes,
                               @RequestParam String login,
                               @RequestParam String password,
                               @ModelAttribute("user-session") UserSession session) {
        if (!session.getLogin().equals(login)) {
            return new RedirectView("/main");
        }
        if (!login.matches("[a-zA-Z0-9]{4,9}")) {
            attributes.addAttribute("invalid_login", true);
            return new RedirectView("/signup");
        }
        try {
            if (usersRepository.findByLogin(login) != null) {
                session.setLogin(null);
                attributes.addAttribute("incorrect_login", true);
                return new RedirectView("/signup");
            }

            usersRepository.createNewUser(login, password);

            return new RedirectView("/main");
        } catch (Throwable e) {
            session.setLogin(null);
            attributes.addAttribute("error", true);
            return new RedirectView("/signup");
        }
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

}
