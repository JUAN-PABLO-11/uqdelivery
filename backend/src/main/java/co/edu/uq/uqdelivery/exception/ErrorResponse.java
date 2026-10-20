package co.edu.uq.uqdelivery.exception;

import java.util.Map;

/** Formato único de error de toda la API. */
public record ErrorResponse(String codigo, String mensaje, Map<String, String> campos) {
    public ErrorResponse(String codigo, String mensaje) {
        this(codigo, mensaje, null);
    }
}
