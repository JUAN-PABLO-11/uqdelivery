package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.CotizacionRequest;
import co.edu.uq.uqdelivery.dto.response.CotizacionResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.Zona;
import co.edu.uq.uqdelivery.repository.ZonaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** Pruebas del contrato. HU-05 agrega aquí las pruebas del cálculo real. */
@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {

    @Mock ZonaRepository zonaRepository;
    @InjectMocks CotizacionService cotizacionService;

    private Zona zona(Long id, boolean activa) {
        Zona z = new Zona("Zona " + id, 4.53, -75.68, activa);
        z.setId(id);
        return z;
    }

    private CotizacionRequest request(Long origenId, Long destinoId) {
        return new CotizacionRequest(origenId, destinoId, 2.5, 20, 30, 40);
    }

    @Test
    void cotizaCuandoAmbasZonasEstanActivas() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona(1L, true)));
        when(zonaRepository.findById(3L)).thenReturn(Optional.of(zona(3L, true)));

        CotizacionResponse r = cotizacionService.cotizar(request(1L, 3L));

        assertNotNull(r.total());
        assertTrue(r.total().signum() > 0);
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
