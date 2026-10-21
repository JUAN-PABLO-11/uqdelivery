package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.CotizacionRequest;
import co.edu.uq.uqdelivery.dto.response.CotizacionResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.Zona;
import co.edu.uq.uqdelivery.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Servicio de cotización, compartido por HU-04 (crear envío) y HU-05 (cotizar).
 *
 * VERSIÓN TEMPORAL: la firma de cotizar() es el contrato acordado y NO se cambia.
 * HU-05 reemplaza el cálculo fijo por la fórmula real:
 *   distanciaKm = haversine(zonaOrigen, zonaDestino)
 *   total = tarifa.base + distanciaKm * tarifa.valorKm + pesoKg * tarifa.valorKg
 * (tarifa vigente; si no hay -> 500 TARIFA_NO_CONFIGURADA).
 */
@Service
@RequiredArgsConstructor
public class CotizacionService {

    private final ZonaRepository zonaRepository;

    @Transactional(readOnly = true)
    public CotizacionResponse cotizar(CotizacionRequest request) {
        Zona origen = zonaEnCobertura(request.zonaOrigenId());
        Zona destino = zonaEnCobertura(request.zonaDestinoId());

        // TODO HU-05: reemplazar estos valores fijos por el cálculo con haversine y la tarifa vigente.
        return new CotizacionResponse(
                5.00,
                new BigDecimal("5000.00"),
                new BigDecimal("6000.00"),
                new BigDecimal("2000.00"),
                new BigDecimal("13000.00"));
    }

    /** La zona debe existir y estar activa; si no, el destino (u origen) está fuera de cobertura. */
    private Zona zonaEnCobertura(Long zonaId) {
        return zonaRepository.findById(zonaId)
                .filter(Zona::isActiva)
                .orElseThrow(() -> new AppException("FUERA_DE_COBERTURA",
                        "Destino fuera de cobertura", HttpStatus.UNPROCESSABLE_ENTITY));
    }
}
