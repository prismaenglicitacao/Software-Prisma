package br.com.softwareprisma.licitacao.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SenhaValidatorImpl.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhaForte {
    String message() default "A senha deve ter no mínimo 8 caracteres, incluindo pelo menos 1 letra maiúscula, 1 letra minúscula e 1 número";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
