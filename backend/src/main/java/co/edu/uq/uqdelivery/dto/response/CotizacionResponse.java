package co.edu.uq.uqdelivery.dto.response;

import java.math.BigDecimal;

/**
 * Contrato acordado entre HU-04 y HU-05. No cambiar los campos sin avisar al equipo.
 * Valores en dinero con 2 decimales (HALF_UP). distanciaKm con 2 decimales.
 */
public record CotizacionResponse(
        double distanciaKm,
        BigDecimal tarifaBase,
        BigDecimal valorDistancia,
        BigDecimal valorPeso,
        BigDecimal total) {
}
