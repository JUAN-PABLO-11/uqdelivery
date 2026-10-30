package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.CotizacionRequest;
import co.edu.uq.uqdelivery.dto.response.CotizacionResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.Tarifa;
import co.edu.uq.uqdelivery.model.Zona;
import co.edu.uq.uqdelivery.repository.TarifaRepository;
import co.edu.uq.uqdelivery.repository.ZonaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** Pruebas del contrato. HU-05 agrega aquí las pruebas del cálculo real. */
@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {

    @Mock ZonaRepository zonaRepository;
    @Mock TarifaRepository tarifaRepository;
    @InjectMocks CotizacionService cotizacionService;

    private Zona zona(Long id, boolean activa) {
        return zona(id, activa, 4.53, -75.68);
    }

    private Zona zona(Long id, boolean activa, double latitud, double longitud) {
        Zona z = new Zona("Zona " + id, latitud, longitud, activa);
        z.setId(id);
        return z;
    }

    private CotizacionRequest request(Long origenId, Long destinoId) {
        return new CotizacionRequest(origenId, destinoId, 2.5, 20, 30, 40);
    }

    private Tarifa tarifaVigente() {
        return new Tarifa(new BigDecimal("5000"), new BigDecimal("1200"), new BigDecimal("800"));
    }

    @Test
    void cotizaCuandoAmbasZonasEstanActivas() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona(1L, true)));
        when(zonaRepository.findById(3L)).thenReturn(Optional.of(zona(3L, true)));
        when(tarifaRepository.findFirstByVigenteTrue()).thenReturn(Optional.of(tarifaVigente()));

        CotizacionResponse r = cotizacionService.cotizar(request(1L, 3L));

        assertEquals(0.0, r.distanciaKm());
        assertEquals(new BigDecimal("5000.00"), r.tarifaBase());
        assertEquals(new BigDecimal("0.00"), r.valorDistancia());
        assertEquals(new BigDecimal("2000.00"), r.valorPeso());
        assertEquals(new BigDecimal("7000.00"), r.total());
    }

    @Test
    void calculaDistanciaHaversineEnKilometros() {
        double distancia = CotizacionService.haversine(0, 0, 0, 1);

        assertEquals(111.19, distancia, 0.01);
    }

    @Test
    void calculaCotizacionCompletaConDistanciaYTarifa() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona(1L, true, 0, 0)));
        when(zonaRepository.findById(2L)).thenReturn(Optional.of(zona(2L, true, 0, 1)));
        when(tarifaRepository.findFirstByVigenteTrue()).thenReturn(Optional.of(tarifaVigente()));

        CotizacionResponse r = cotizacionService.cotizar(request(1L, 2L));

        assertEquals(111.19, r.distanciaKm());
        assertEquals(new BigDecimal("5000.00"), r.tarifaBase());
        assertEquals(new BigDecimal("133428.00"), r.valorDistancia());
        assertEquals(new BigDecimal("2000.00"), r.valorPeso());
        assertEquals(new BigDecimal("140428.00"), r.total());
    }

    @Test
    void fallaCuandoNoHayTarifaVigente() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona(1L, true)));
        when(zonaRepository.findById(3L)).thenReturn(Optional.of(zona(3L, true)));
        when(tarifaRepository.findFirstByVigenteTrue()).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> cotizacionService.cotizar(request(1L, 3L)));

        assertEquals("TARIFA_NO_CONFIGURADA", e.getCodigo());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getEstado());
    }

    @Test
    void destinoInactivoEstaFueraDeCobertura() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona(1L, true)));
        when(zonaRepository.findById(6L)).thenReturn(Optional.of(zona(6L, false)));

        AppException e = assertThrows(AppException.class, () -> cotizacionService.cotizar(request(1L, 6L)));

        assertEquals("FUERA_DE_COBERTURA", e.getCodigo());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.getEstado());
    }

    @Test
    void destinoInexistenteEstaFueraDeCobertura() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona(1L, true)));
        when(zonaRepository.findById(99L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> cotizacionService.cotizar(request(1L, 99L)));

        assertEquals("FUERA_DE_COBERTURA", e.getCodigo());
    }

    @Test
    void origenInactivoTambienEstaFueraDeCobertura() {
        when(zonaRepository.findById(6L)).thenReturn(Optional.of(zona(6L, false)));

        AppException e = assertThrows(AppException.class, () -> cotizacionService.cotizar(request(6L, 1L)));

        assertEquals("FUERA_DE_COBERTURA", e.getCodigo());
    }
}
