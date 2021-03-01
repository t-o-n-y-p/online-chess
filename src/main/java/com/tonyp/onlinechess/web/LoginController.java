package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@SessionAttributes("user-session")
public class LoginController {

    private UsersDao usersDao;

    public LoginController(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    @GetMapping("/login")
    public String login(@ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() != null) {
            return "redirect:main";
        }
        return "login";
    }

    @PostMapping("/login")
    public RedirectView login(@RequestParam String login,
                              @RequestParam String password,
                              @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() != null) {
            return new RedirectView("/main");
        }
        if (usersDao.findByLoginAndPassword(login, password) != null) {
            session.setLogin(login);
            return new RedirectView("/main");
        }
        return new RedirectView("/login");
    }

    @PostMapping("/logout")
    public RedirectView logout(@ModelAttribute("user-session") UserSession session) {
        session.setLogin(null);
        return new RedirectView("/login");
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

}
