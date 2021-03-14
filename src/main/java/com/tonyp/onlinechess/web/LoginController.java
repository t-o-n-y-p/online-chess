package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.validation.LoginForm;
import com.tonyp.onlinechess.validation.SignupForm;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;

@Controller
@SessionAttributes("user-session")
@AllArgsConstructor
public class LoginController {

    private final UsersRepository usersRepository;

    @GetMapping("/login")
    public String getLogin(Model model,
                           LoginForm loginForm,
                           @RequestParam(defaultValue = "false", name = "force_logout") boolean forceLogout,
                           @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() != null) {
            return "redirect:main";
        }
        model.addAttribute("forceLogout", forceLogout);
        return "_login";
    }

    @PostMapping("/login")
    public String postLogin(Model model,
                            @Valid LoginForm loginForm,
                            BindingResult bindingResult,
                            @ModelAttribute("user-session") UserSession session) {
        if (!session.getLogin().equals(loginForm.getLogin())) {
            return "redirect:/main";
        }
        if (bindingResult.hasErrors()) {
            session.setLogin(null);
            return "_login";
        }
        try {
            User found = usersRepository.findByLogin(loginForm.getLogin());
            if (found == null) {
                session.setLogin(null);
                bindingResult.addError(new FieldError("loginForm", "login", "Login is incorrect."));
                return "_login";
            }
            if (!found.getPassword().equals(loginForm.getPassword())) {
                session.setLogin(null);
                bindingResult.addError(new FieldError("loginForm", "password", "Password is incorrect."));
                return "_login";
            }
            return "redirect:/main";
        } catch (Throwable e) {
            session.setLogin(null);
            model.addAttribute("error", true);
            return "_login";
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
