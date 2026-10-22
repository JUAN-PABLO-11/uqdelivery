package co.edu.uq.uqdelivery.dto.response;

import co.edu.uq.uqdelivery.model.Envio;
import co.edu.uq.uqdelivery.model.EstadoEnvio;
import co.edu.uq.uqdelivery.model.Zona;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * HU-04: envío tal como lo ve el cliente. Las zonas van por nombre (nunca la entidad).
 * Llamar a de() dentro de una transacción: las zonas se cargan de forma perezosa (open-in-view está apagado).
 */
public record EnvioResponse(
        Long id,
        String codigo,
        EstadoEnvio estado,
        String remitente,
        String destinatario,
        String direccionOrigen,
        String direccionDestino,
        String zonaOrigen,
        String zonaDestino,
        double pesoKg,
        double altoCm,
        double anchoCm,
        double largoCm,
        double distanciaKm,
        BigDecimal valorTotal,
        LocalDateTime creadoEn) {

    public static EnvioResponse de(Envio e) {
        return new EnvioResponse(
                e.getId(),
                e.getCodigo(),
                e.getEstado(),
                e.getRemitente(),
                e.getDestinatario(),
                e.getDireccionOrigen(),
                e.getDireccionDestino(),
                nombre(e.getZonaOrigen()),
                nombre(e.getZonaDestino()),
                e.getPesoKg(),
                e.getAltoCm(),
                e.getAnchoCm(),
                e.getLargoCm(),
                e.getDistanciaKm(),
                e.getValorTotal(),
                e.getCreadoEn());
    }

    private static String nombre(Zona zona) {
        return zona == null ? null : zona.getNombre();
    }
}
