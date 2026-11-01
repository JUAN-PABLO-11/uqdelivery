package co.edu.uq.uqdelivery.dto.request;

import jakarta.validation.constraints.NotNull;

public record AsignarRequest(
        @NotNull(message = "El ID del repartidor es obligatorio")
        Long repartidorId
) {}
