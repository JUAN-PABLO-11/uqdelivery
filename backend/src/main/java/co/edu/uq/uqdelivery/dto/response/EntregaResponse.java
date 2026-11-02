package co.edu.uq.uqdelivery.dto.response;

import co.edu.uq.uqdelivery.model.EstadoEnvio;
import java.time.LocalDateTime;

/** HU-06 / HU-10: entrega tal como la ve el repartidor. */
public record EntregaResponse(
        Long id,
        String codigo,
        EstadoEnvio estado,
        String direccionDestino,
        String zonaDestino,
        String destinatario,
        LocalDateTime creadoEn
) {}
