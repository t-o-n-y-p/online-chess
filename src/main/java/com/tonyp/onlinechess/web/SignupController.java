package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.validation.SignupForm;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@AllArgsConstructor
@Slf4j
public class SignupController {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SIGNUP_PAGE = "_signup";

    @GetMapping("/signup")
    public String getSignup(SignupForm signupForm, Authentication authentication) {
        if (authentication != null) {
            return "redirect:/app/main";
        }
        return SIGNUP_PAGE;
    }

    @PostMapping("/signup")
    public String postSignup(HttpServletRequest request,
                             Model model,
                             @Valid SignupForm signupForm,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return SIGNUP_PAGE;
        }
        try {
            usersRepository.createNewUser(signupForm.getLogin(), passwordEncoder.encode(signupForm.getPassword()));
            request.login(signupForm.getLogin(), signupForm.getPassword());
            return "redirect:/app/main";
        } catch (JpaSystemException e) {
            log.error("Sign up error", e);
            bindingResult.addError(new FieldError("signupForm", "login", "User with this login already exists."));
            return SIGNUP_PAGE;
        } catch (Exception e) {
            log.error("Sign up error", e);
            model.addAttribute("error", true);
            return SIGNUP_PAGE;
        }
    }
}
