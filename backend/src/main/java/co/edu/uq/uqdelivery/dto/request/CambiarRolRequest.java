package co.edu.uq.uqdelivery.dto.request;

import co.edu.uq.uqdelivery.model.Rol;
import jakarta.validation.constraints.NotNull;

public record CambiarRolRequest(

        @NotNull(message = "El rol es obligatorio")
        Rol rol

) {
}