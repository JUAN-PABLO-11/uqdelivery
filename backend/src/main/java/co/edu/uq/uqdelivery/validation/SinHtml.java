package co.edu.uq.uqdelivery.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Rechaza etiquetas HTML, "javascript:" y atributos on*= (protección XSS). Úsala en todo texto libre. */
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Retention(RUNTIME)
@Constraint(validatedBy = SinHtmlValidator.class)
public @interface SinHtml {
    String message() default "Contiene caracteres no permitidos";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
