package co.edu.uq.uqdelivery.dto.request;

import co.edu.uq.uqdelivery.model.EstadoEnvio;
import co.edu.uq.uqdelivery.validation.SinHtml;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoRequest(
        @NotNull(message = "El estado es obligatorio") EstadoEnvio estado,
        @Size(max = 255) @SinHtml String detalle) {
}
