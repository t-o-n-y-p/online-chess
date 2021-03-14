package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.validation.SignupForm;
import lombok.AllArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
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
            return "redirect:/main";
        }
        if (bindingResult.hasErrors()) {
            session.setLogin(null);
            return "_signup";
        }
        try {
            usersRepository.createNewUser(signupForm.getLogin(), signupForm.getPassword());
            return "redirect:/main";
        } catch (JpaSystemException e) {
            session.setLogin(null);
            bindingResult.addError(new FieldError("signupForm", "login", "User with this login already exists."));
            return "_signup";
        } catch (Throwable e) {
            session.setLogin(null);
            model.addAttribute("error", true);
            return "_signup";
        }
    }

    @ModelAttribute("user-session")
    public UserSession createUserSession() {
        return new UserSession();
    }

}
