package com.tonyp.onlinechess.validation;

import lombok.*;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class LoginForm {

    @NotBlank(message = "Login must not be empty.")
    private String login;

    @NotBlank(message = "Password must not be empty.")
    private String password;

}
