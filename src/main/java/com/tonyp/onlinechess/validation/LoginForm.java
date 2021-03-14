package com.tonyp.onlinechess.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginForm {

    @NotBlank(message = "Login must not be empty.")
    private String login;

    @NotBlank(message = "Password must not be empty.")
    private String password;

}
