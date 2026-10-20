package co.edu.uq.uqdelivery.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class SinHtmlValidator implements ConstraintValidator<SinHtml, String> {

    private static final Pattern PELIGROSO =
            Pattern.compile("<[^>]*>|javascript:|on\\w+\\s*=", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext ctx) {
        return valor == null || !PELIGROSO.matcher(valor).find();
    }
}
