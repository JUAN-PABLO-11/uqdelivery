package co.edu.uq.uqdelivery.dto.request;

import co.edu.uq.uqdelivery.model.TipoNovedad;
import co.edu.uq.uqdelivery.validation.SinHtml;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearNovedadRequest(
        @NotNull(message = "El tipo de novedad es obligatorio")
        TipoNovedad tipo,

        @NotNull(message = "La descripción es obligatoria")
        @Size(min = 10, message = "La descripción debe tener al menos 10 caracteres")
        @SinHtml(message = "La descripción no puede contener HTML o scripts")
        String descripcion,

        @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
        @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
        Double latitud,

        @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
        @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
        Double longitud
) {}