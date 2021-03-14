package com.tonyp.onlinechess.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordsMatchValidator.class)
public @interface PasswordsMatch {

    String message() default "Passwords do not match.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
