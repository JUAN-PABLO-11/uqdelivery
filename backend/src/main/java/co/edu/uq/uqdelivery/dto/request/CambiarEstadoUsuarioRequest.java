package co.edu.uq.uqdelivery.dto.request;

import jakarta.validation.constraints.NotNull;

/** HU-02: activar (true) o desactivar (false) una cuenta. */
public record CambiarEstadoUsuarioRequest(
        @NotNull(message = "Debe indicar si el usuario queda activo")
        Boolean activo
) {
}
