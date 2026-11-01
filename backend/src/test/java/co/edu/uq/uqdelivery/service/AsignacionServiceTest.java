package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.AsignarRequest;
import co.edu.uq.uqdelivery.dto.request.ReasignarRequest;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.*;
import co.edu.uq.uqdelivery.repository.AsignacionRepository;
import co.edu.uq.uqdelivery.repository.EnvioRepository;
import co.edu.uq.uqdelivery.repository.HistorialEstadoRepository;
import co.edu.uq.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** HU-06: asignación y reasignación de repartidores. */
@ExtendWith(MockitoExtension.class)
class AsignacionServiceTest {

    @Mock EnvioRepository envioRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock AsignacionRepository asignacionRepository;
    @Mock HistorialEstadoRepository historialRepository;
    @Mock EstadoService estadoService;
    @InjectMocks AsignacionService asignacionService;

    private final Usuario admin = usuario(1L, "Admin", Rol.ADMIN);
    private final Usuario carlos = usuario(10L, "Carlos", Rol.REPARTIDOR);
    private final Usuario diana = usuario(11L, "Diana", Rol.REPARTIDOR);

    private static Usuario usuario(Long id, String nombre, Rol rol) {
        Usuario u = new Usuario(nombre, nombre.toLowerCase() + "@uq.com", "hash", rol);
        u.setId(id);
        return u;
    }

    private Envio envio(EstadoEnvio estado) {
        Envio e = new Envio();
        e.setId(5L);
        e.setEstado(estado);
        when(envioRepository.findById(5L)).thenReturn(Optional.of(e));
        return e;
    }

    // ---------- asignar (criterio 1)

    @Test
    void asignarEnvioRegistradoCreaAsignacionYPasaAAsignado() {
        envio(EstadoEnvio.REGISTRADO);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(carlos));

        asignacionService.asignarEnvio(5L, new AsignarRequest(10L), admin);

        ArgumentCaptor<Asignacion> captor = ArgumentCaptor.forClass(Asignacion.class);
        verify(asignacionRepository).save(captor.capture());
        assertEquals(carlos, captor.getValue().getRepartidor());
        assertTrue(captor.getValue().isActiva());
        verify(estadoService).cambiarEstado(eq(5L), eq(EstadoEnvio.ASIGNADO), eq(admin), anyString());
    }

    @Test
    void noSePuedeAsignarUnEnvioQueNoEstaRegistrado() {
        envio(EstadoEnvio.EN_RUTA);

        AppException e = assertThrows(AppException.class,
                () -> asignacionService.asignarEnvio(5L, new AsignarRequest(10L), admin));

        assertEquals("TRANSICION_INVALIDA", e.getCodigo());
        verify(asignacionRepository, never()).save(any());
    }

    @Test
    void noSePuedeAsignarAUnUsuarioQueNoEsRepartidor() {
        envio(EstadoEnvio.REGISTRADO);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        AppException e = assertThrows(AppException.class,
                () -> asignacionService.asignarEnvio(5L, new AsignarRequest(1L), admin));

        assertEquals("REPARTIDOR_NO_DISPONIBLE", e.getCodigo());
    }

    @Test
    void noSePuedeAsignarAUnRepartidorInactivo() {
        envio(EstadoEnvio.REGISTRADO);
        carlos.setActivo(false);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(carlos));

        AppException e = assertThrows(AppException.class,
                () -> asignacionService.asignarEnvio(5L, new AsignarRequest(10L), admin));

        assertEquals("REPARTIDOR_NO_DISPONIBLE", e.getCodigo());
    }

    @Test
    void envioInexistenteDevuelveNoEncontrado() {
        when(envioRepository.findById(5L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class,
                () -> asignacionService.asignarEnvio(5L, new AsignarRequest(10L), admin));

        assertEquals("ENVIO_NO_ENCONTRADO", e.getCodigo());
    }

    // ---------- reasignar (criterio 2)

    @Test
    void reasignarDesactivaLaAsignacionAnteriorYRegistraElHistorial() {
        Envio e = envio(EstadoEnvio.ASIGNADO);
        Asignacion actual = new Asignacion(e, carlos, admin, "Asignación inicial");
        when(asignacionRepository.findByEnvioIdAndActivaTrue(5L)).thenReturn(Optional.of(actual));
        when(usuarioRepository.findById(11L)).thenReturn(Optional.of(diana));

        asignacionService.reasignarEnvio(5L, new ReasignarRequest(11L, "Carlos está incapacitado"), admin);

        assertFalse(actual.isActiva());
        ArgumentCaptor<Asignacion> asignaciones = ArgumentCaptor.forClass(Asignacion.class);
        verify(asignacionRepository, times(2)).save(asignaciones.capture());
        Asignacion nueva = asignaciones.getAllValues().get(1);
        assertEquals(diana, nueva.getRepartidor());
        assertTrue(nueva.isActiva());

        ArgumentCaptor<HistorialEstado> historial = ArgumentCaptor.forClass(HistorialEstado.class);
        verify(historialRepository).save(historial.capture());
        assertEquals(admin, historial.getValue().getActor());
        assertTrue(historial.getValue().getDetalle().contains("Carlos está incapacitado"));
        assertEquals(EstadoEnvio.ASIGNADO, e.getEstado());
    }

    @Test
    void noSePuedeReasignarAlMismoRepartidor() {
        Envio e = envio(EstadoEnvio.ASIGNADO);
        when(asignacionRepository.findByEnvioIdAndActivaTrue(5L))
                .thenReturn(Optional.of(new Asignacion(e, carlos, admin, "inicial")));

        AppException ex = assertThrows(AppException.class,
                () -> asignacionService.reasignarEnvio(5L, new ReasignarRequest(10L, "Motivo válido"), admin));

        assertEquals("MISMO_REPARTIDOR", ex.getCodigo());
    }

    @Test
    void soloSeReasignanEnviosEnEstadoAsignado() {
        envio(EstadoEnvio.EN_RUTA);

        AppException ex = assertThrows(AppException.class,
                () -> asignacionService.reasignarEnvio(5L, new ReasignarRequest(11L, "Motivo válido"), admin));

        assertEquals("TRANSICION_INVALIDA", ex.getCodigo());
    }

    @Test
    void listarRepartidoresDisponibles() {
        when(usuarioRepository.findByRolAndActivoTrueAndDisponibleTrue(Rol.REPARTIDOR)).thenReturn(List.of(carlos, diana));

        var lista = asignacionService.listarRepartidoresDisponibles();

        assertEquals(2, lista.size());
        assertEquals("Carlos", lista.get(0).nombre());
    }
}
