package com.tonyp.onlinechess.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@PasswordsMatch
public class SignupForm {

    @Pattern(regexp = "[a-zA-Z0-9]{4,9}", message = "Login must contain 4-9 characters: a-z, A-Z, or 0-9.")
    private String login;

    @NotBlank(message = "Password must not be empty.")
    private String password;

    private String repeatPassword;

}
