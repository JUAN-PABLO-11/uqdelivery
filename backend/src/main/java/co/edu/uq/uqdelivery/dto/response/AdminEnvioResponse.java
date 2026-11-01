package co.edu.uq.uqdelivery.dto.response;

import co.edu.uq.uqdelivery.model.EstadoEnvio;

public record AdminEnvioResponse(
        Long id,
        String codigo,
        EstadoEnvio estado,
        String destinatario,
        String direccionDestino,
        String cliente,
        String repartidor // Nombre del repartidor o null si aún no tiene
) {}
