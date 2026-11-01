package co.edu.uq.uqdelivery.dto.response;

import co.edu.uq.uqdelivery.model.EstadoEnvio;

import java.time.LocalDateTime;

/** HU-06: envío tal como lo ve el administrador. repartidorId/repartidor son null si aún no tiene. */
public record AdminEnvioResponse(
        Long id,
        String codigo,
        EstadoEnvio estado,
        String destinatario,
        String direccionDestino,
        String zonaDestino,
        String cliente,
        Long repartidorId,
        String repartidor,
        LocalDateTime creadoEn
) {}
