package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.validation.SignupForm;
import lombok.AllArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.validation.Valid;

@Controller
@SessionAttributes("user-session")
@AllArgsConstructor
public class SignupController {

    private final UsersRepository usersRepository;

    @GetMapping("/signup")
    public String getSignup(SignupForm signupForm,
                            @ModelAttribute("user-session") UserSession session) {
        if (session.getLogin() != null) {
            return "redirect:main";
        }
        return "_signup";
    }

    @PostMapping("/signup")
    public String postSignup(Model model,
                             @Valid SignupForm signupForm,
                             BindingResult bindingResult,
                             @ModelAttribute("user-session") UserSession session) {
        if (!session.getLogin().equals(signupForm.getLogin())) {
            return "redirect:main";
        }
        try {
            if (bindingResult.hasErrors()) {
                session.setLogin(null);
                return "_signup";
            }

            usersRepository.createNewUser(signupForm.getLogin(), signupForm.getPassword());
            return "redirect:main";
        } catch (Throwable e) {
            session.setLogin(null);
            while (e.getCause() != null && !e.getCause().equals(e)) {
                e = e.getCause();
                if (e.getClass().equals(ConstraintViolationException.class)) {
                    bindingResult.addError(new FieldError("form", "login", "User with this login already exists."));
                    return "_signup";
                }
            }
            model.addAttribute("error", true);
            return "_signup";
        }
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

}
