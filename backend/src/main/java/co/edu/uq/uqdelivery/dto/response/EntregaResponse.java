package co.edu.uq.uqdelivery.dto.response;

import co.edu.uq.uqdelivery.model.EstadoEnvio;
import java.time.LocalDateTime;

public record EntregaResponse(
        Long id,
        String codigo,
        EstadoEnvio estado,
        String direccionDestino,
        String destinatario,
        LocalDateTime creadoEn
) {}
