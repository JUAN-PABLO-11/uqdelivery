package co.edu.uq.uqdelivery.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SinHtmlValidatorTest {

    private final SinHtmlValidator validator = new SinHtmlValidator();

    @ParameterizedTest
    @ValueSource(strings = {"<script>alert(1)</script>", "<b>hola</b>", "javascript:alert(1)", "img onerror=alert(1)"})
    void rechazaTextoPeligroso(String texto) {
        assertFalse(validator.isValid(texto, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Calle 21 # 15-30", "Apto 301, Torre B", "Entregar en portería"})
    void aceptaTextoNormal(String texto) {
        assertTrue(validator.isValid(texto, null));
    }

    @Test
    void aceptaNull() {
        assertTrue(validator.isValid(null, null));
    }
}
