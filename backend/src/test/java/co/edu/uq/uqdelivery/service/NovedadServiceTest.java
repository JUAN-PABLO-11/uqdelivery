package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.CrearNovedadRequest;
import co.edu.uq.uqdelivery.dto.response.NovedadResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.*;
import co.edu.uq.uqdelivery.repository.AsignacionRepository;
import co.edu.uq.uqdelivery.repository.EnvioRepository;
import co.edu.uq.uqdelivery.repository.NovedadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NovedadServiceTest {

    @Mock
    private NovedadRepository novedadRepository;
    @Mock
    private EnvioRepository envioRepository;
    @Mock
    private AsignacionRepository asignacionRepository;
    @Mock
    private EstadoService estadoService;

    @InjectMocks
    private NovedadService novedadService;

    private Usuario repartidor;
    private Envio envio;

    @BeforeEach
    void setUp() {
        repartidor = new Usuario("Pedro Repartidor", "repartidor1@uq.com", "pass123", Rol.REPARTIDOR);
        repartidor.setId(10L);

        envio = new Envio();
        envio.setId(100L);
        envio.setEstado(EstadoEnvio.EN_RUTA);
    }

    @Test
    void registrarNovedad_Exitoso() {
        CrearNovedadRequest request = new CrearNovedadRequest(
                TipoNovedad.DESTINATARIO_AUSENTE, "El destinatario no contestó la llamada", 4.5, -75.6
        );

        when(envioRepository.findById(100L)).thenReturn(Optional.of(envio));
        when(asignacionRepository.existsByEnvioIdAndRepartidorIdAndActivaTrue(100L, 10L)).thenReturn(true);
        when(novedadRepository.save(any(Novedad.class))).thenAnswer(invocation -> {
            Novedad n = invocation.getArgument(0);
            n.setId(1L);
            return n;
        });

        NovedadResponse response = novedadService.registrarNovedad(100L, request, repartidor);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(100L, response.envioId());
        assertEquals(TipoNovedad.DESTINATARIO_AUSENTE, response.tipo());

        verify(estadoService).cambiarEstado(eq(100L), eq(EstadoEnvio.CON_NOVEDAD), eq(repartidor), anyString());
    }

    @Test
    void registrarNovedad_LanzaExcepcion_CuandoEnvioNoEstaEnRuta() {
        envio.setEstado(EstadoEnvio.ASIGNADO);
        CrearNovedadRequest request = new CrearNovedadRequest(
                TipoNovedad.PAQUETE_DANADO, "El empaque viene roto", null, null
        );

        when(envioRepository.findById(100L)).thenReturn(Optional.of(envio));
        when(asignacionRepository.existsByEnvioIdAndRepartidorIdAndActivaTrue(100L, 10L)).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> novedadService.registrarNovedad(100L, request, repartidor));
        assertEquals("TRANSICION_INVALIDA", ex.getCodigo());
        verify(estadoService, never()).cambiarEstado(any(), any(), any(), any());
    }

    @Test
    void registrarNovedad_LanzaExcepcion_CuandoNoPerteneceAlRepartidor() {
        CrearNovedadRequest request = new CrearNovedadRequest(
                TipoNovedad.DIRECCION_INCORRECTA, "No existe el número de casa", 4.5, -75.6
        );

        when(envioRepository.findById(100L)).thenReturn(Optional.of(envio));
        when(asignacionRepository.existsByEnvioIdAndRepartidorIdAndActivaTrue(100L, 10L)).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> novedadService.registrarNovedad(100L, request, repartidor));
        assertEquals("PROHIBIDO", ex.getCodigo());
    }
}