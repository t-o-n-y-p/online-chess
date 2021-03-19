package com.tonyp.onlinechess.web;

import com.tonyp.onlinechess.dao.UsersRepository;
import com.tonyp.onlinechess.model.User;
import com.tonyp.onlinechess.validation.SignupForm;
import lombok.AllArgsConstructor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
@AllArgsConstructor
public class SignupController {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/signup")
    public String getSignup(SignupForm signupForm, Authentication authentication) {
        if (authentication != null) {
            return "redirect:/app/main";
        }
        return "_signup";
    }

    @PostMapping("/signup")
    public String postSignup(HttpServletRequest request,
                             Model model,
                             @Valid SignupForm signupForm,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "_signup";
        }
        try {
            usersRepository.createNewUser(signupForm.getLogin(), passwordEncoder.encode(signupForm.getPassword()));
            request.login(signupForm.getLogin(), signupForm.getPassword());
            return "redirect:/app/main";
        } catch (JpaSystemException e) {
            bindingResult.addError(new FieldError("signupForm", "login", "User with this login already exists."));
            return "_signup";
        } catch (Throwable e) {
            model.addAttribute("error", true);
            return "_signup";
        }
    }
}
