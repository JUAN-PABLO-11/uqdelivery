package co.edu.uq.uqdelivery.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Excepción de negocio. Lánzala desde los services; GlobalExceptionHandler la convierte en JSON. */
@Getter
public class AppException extends RuntimeException {
    private final String codigo;
    private final HttpStatus estado;

    public AppException(String codigo, String mensaje, HttpStatus estado) {
        super(mensaje);
        this.codigo = codigo;
        this.estado = estado;
    }

    public static AppException noEncontrado(String mensaje) {
        return new AppException("NO_ENCONTRADO", mensaje, HttpStatus.NOT_FOUND);
    }

    public static AppException prohibido() {
        return new AppException("PROHIBIDO", "Acceso no autorizado", HttpStatus.FORBIDDEN);
    }
}
