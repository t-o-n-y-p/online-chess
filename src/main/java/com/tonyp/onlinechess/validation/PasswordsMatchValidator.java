package com.tonyp.onlinechess.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, SignupForm> {
    @Override
    public void initialize(PasswordsMatch constraintAnnotation) {
    }

    @Override
    public boolean isValid(SignupForm signupForm, ConstraintValidatorContext constraintValidatorContext) {
        return Objects.equals(signupForm.getPassword(), signupForm.getRepeatPassword());
    }
}
