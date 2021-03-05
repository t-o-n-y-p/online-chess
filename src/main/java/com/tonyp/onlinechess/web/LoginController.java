package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersDao;
import com.tonyp.onlinechess.model.User;
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
    public String login(Model model,
                        @RequestParam(defaultValue = "false") boolean error,
                        @RequestParam(defaultValue = "false", name = "force_logout") boolean forceLogout,
                        @RequestParam(defaultValue = "false", name = "incorrect_login") boolean incorrectLogin,
                        @RequestParam(defaultValue = "false", name = "incorrect_password") boolean incorrectPassword,
                        @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() != null) {
            return "redirect:main";
        }
        model.addAttribute("error", error);
        model.addAttribute("forceLogout", forceLogout);
        model.addAttribute("incorrectLogin", incorrectLogin);
        model.addAttribute("incorrectPassword", incorrectPassword);
        return "_login";
    }

    @PostMapping("/login")
    public RedirectView login(RedirectAttributes attributes,
                              @RequestParam String login,
                              @RequestParam String password,
                              @ModelAttribute("user-session") UserSession session) {
        if (!session.getLogin().equals(login)) {
            return new RedirectView("/main");
        }
        try {
            User found = usersDao.findByLogin(login);
            if (found == null) {
                session.setLogin(null);
                attributes.addAttribute("incorrect_login", true);
                return new RedirectView("/login");
            }
            if (!found.getPassword().equals(password)) {
                session.setLogin(null);
                attributes.addAttribute("incorrect_password", true);
                return new RedirectView("/login");
            }
            return new RedirectView("/main");
        } catch (Throwable e) {
            session.setLogin(null);
            attributes.addAttribute("error", true);
            return new RedirectView("/login");
        }
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
