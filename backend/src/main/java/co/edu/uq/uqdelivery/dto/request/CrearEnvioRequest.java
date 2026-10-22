package co.edu.uq.uqdelivery.dto.request;

import co.edu.uq.uqdelivery.validation.SinHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * HU-04: datos para crear un envío.
 * El cliente sale del token y el valor/distancia los calcula el backend: no vienen aquí.
 */
public record CrearEnvioRequest(
        @NotBlank(message = "El remitente es obligatorio")
        @Size(max = 150, message = "Máximo 150 caracteres")
        @SinHtml String remitente,

        @NotBlank(message = "El destinatario es obligatorio")
        @Size(max = 150, message = "Máximo 150 caracteres")
        @SinHtml String destinatario,

        @NotBlank(message = "La dirección de origen es obligatoria")
        @Size(max = 255, message = "Máximo 255 caracteres")
        @SinHtml String direccionOrigen,

        @NotBlank(message = "La dirección de destino es obligatoria")
        @Size(max = 255, message = "Máximo 255 caracteres")
        @SinHtml String direccionDestino,

        @NotNull(message = "La zona de origen es obligatoria") Long zonaOrigenId,
        @NotNull(message = "La zona de destino es obligatoria") Long zonaDestinoId,

        @Positive(message = "Debe ser mayor a 0") double pesoKg,
        @Positive(message = "Debe ser mayor a 0") double altoCm,
        @Positive(message = "Debe ser mayor a 0") double anchoCm,
        @Positive(message = "Debe ser mayor a 0") double largoCm) {

    /** Datos que necesita CotizacionService (HU-05) para calcular distancia y valor. */
    public CotizacionRequest aCotizacion() {
        return new CotizacionRequest(zonaOrigenId, zonaDestinoId, pesoKg, altoCm, anchoCm, largoCm);
    }
}
