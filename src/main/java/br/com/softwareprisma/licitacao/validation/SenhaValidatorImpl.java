package br.com.softwareprisma.licitacao.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaValidatorImpl implements ConstraintValidator<SenhaForte, String> {

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context) {
        if (senha == null || senha.isEmpty()) {
            return false;
        }

        if (senha.length() < 8) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("A senha deve ter no mínimo 8 caracteres.")
                   .addConstraintViolation();
            return false;
        }

        boolean temMaiuscula = !senha.equals(senha.toLowerCase());
        boolean temMinuscula = !senha.equals(senha.toUpperCase());
        boolean temNumero = senha.matches(".*\\d.*");

        if (!temMaiuscula) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("A senha deve possuir pelo menos uma letra maiúscula.")
                   .addConstraintViolation();
            return false;
        }
        if (!temMinuscula) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("A senha deve possuir pelo menos uma letra minúscula.")
                   .addConstraintViolation();
            return false;
        }
        if (!temNumero) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("A senha deve possuir pelo menos um número.")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}
