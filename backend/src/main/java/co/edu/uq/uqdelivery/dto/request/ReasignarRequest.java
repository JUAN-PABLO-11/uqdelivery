package co.edu.uq.uqdelivery.dto.request;

import co.edu.uq.uqdelivery.validation.SinHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReasignarRequest(
        @NotNull(message = "El ID del nuevo repartidor es obligatorio")
        Long repartidorId,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(min = 5, message = "El motivo debe tener al menos 5 caracteres")
        @SinHtml
        String motivo
) {}
