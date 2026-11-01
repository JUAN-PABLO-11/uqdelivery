package co.edu.uq.uqdelivery.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Contrato acordado entre HU-04 y HU-05. No cambiar los campos sin avisar al equipo.
 * Las dimensiones solo se validan; no afectan el precio.
 */
public record CotizacionRequest(
        @NotNull(message = "La zona de origen es obligatoria") Long zonaOrigenId,
        @NotNull(message = "La zona de destino es obligatoria") Long zonaDestinoId,
        @Positive(message = "Debe ser mayor a 0") double pesoKg,
        @Positive(message = "Debe ser mayor a 0") double altoCm,
        @Positive(message = "Debe ser mayor a 0") double anchoCm,
        @Positive(message = "Debe ser mayor a 0") double largoCm) {
}
