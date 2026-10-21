package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.CotizacionRequest;
import co.edu.uq.uqdelivery.dto.response.CotizacionResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.Tarifa;
import co.edu.uq.uqdelivery.model.Zona;
import co.edu.uq.uqdelivery.repository.TarifaRepository;
import co.edu.uq.uqdelivery.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio de cotización, compartido por HU-04 (crear envío) y HU-05 (cotizar).
 *
 * La firma de cotizar() es el contrato compartido con HU-04 y no debe cambiarse
 * sin coordinarlo con la historia de creación de envíos. La fórmula aplicada es:
 *   distanciaKm = haversine(zonaOrigen, zonaDestino)
 *   total = tarifa.base + distanciaKm * tarifa.valorKm + pesoKg * tarifa.valorKg
 * La cotización solo calcula y devuelve datos; no crea ni modifica envíos.
 */
@Service
@RequiredArgsConstructor
public class CotizacionService {

    private final ZonaRepository zonaRepository;
    private final TarifaRepository tarifaRepository;

    private static final double RADIO_TIERRA_KM = 6371.0;
    private static final int ESCALA_MONETARIA = 2;

    @Transactional(readOnly = true)
    public CotizacionResponse cotizar(CotizacionRequest request) {
        Zona origen = zonaEnCobertura(request.zonaOrigenId());
        Zona destino = zonaEnCobertura(request.zonaDestinoId());
        Tarifa tarifa = tarifaRepository.findFirstByVigenteTrue()
            .orElseThrow(() -> new AppException("TARIFA_NO_CONFIGURADA",
                "No existe una tarifa vigente para calcular la cotización", HttpStatus.INTERNAL_SERVER_ERROR));

        double distanciaKm = redondearDistancia(haversine(
            origen.getLatitud(), origen.getLongitud(),
            destino.getLatitud(), destino.getLongitud()));
        BigDecimal tarifaBase = dinero(tarifa.getBase());
        BigDecimal valorDistancia = dinero(BigDecimal.valueOf(distanciaKm)
            .multiply(tarifa.getValorKm()));
        BigDecimal valorPeso = dinero(BigDecimal.valueOf(request.pesoKg())
            .multiply(tarifa.getValorKg()));
        BigDecimal total = dinero(tarifaBase.add(valorDistancia).add(valorPeso));

        return new CotizacionResponse(distanciaKm, tarifaBase, valorDistancia, valorPeso, total);
    }

    /** Calcula la distancia de superficie entre dos coordenadas geográficas en kilómetros. */
    public static double haversine(double latitudOrigen, double longitudOrigen,
                                   double latitudDestino, double longitudDestino) {
        double diferenciaLatitud = Math.toRadians(latitudDestino - latitudOrigen);
        double diferenciaLongitud = Math.toRadians(longitudDestino - longitudOrigen);
        double latitudOrigenRad = Math.toRadians(latitudOrigen);
        double latitudDestinoRad = Math.toRadians(latitudDestino);

        double a = Math.pow(Math.sin(diferenciaLatitud / 2), 2)
            + Math.cos(latitudOrigenRad) * Math.cos(latitudDestinoRad)
            * Math.pow(Math.sin(diferenciaLongitud / 2), 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(a));
    }

    private static double redondearDistancia(double distanciaKm) {
        return BigDecimal.valueOf(distanciaKm)
                .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
    }

    /** La zona debe existir y estar activa; si no, el destino (u origen) está fuera de cobertura. */
    private Zona zonaEnCobertura(Long zonaId) {
        return zonaRepository.findById(zonaId)
                .filter(Zona::isActiva)
                .orElseThrow(() -> new AppException("FUERA_DE_COBERTURA",
                        "Destino fuera de cobertura", HttpStatus.UNPROCESSABLE_ENTITY));
    }
}
