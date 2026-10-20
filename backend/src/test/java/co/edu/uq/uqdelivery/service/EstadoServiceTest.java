package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.*;
import co.edu.uq.uqdelivery.repository.AsignacionRepository;
import co.edu.uq.uqdelivery.repository.EnvioRepository;
import co.edu.uq.uqdelivery.repository.HistorialEstadoRepository;
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
class EstadoServiceTest {

    @Mock EnvioRepository envioRepository;
    @Mock HistorialEstadoRepository historialRepository;
    @Mock AsignacionRepository asignacionRepository;
    @InjectMocks EstadoService estadoService;

    private Envio envioEn(EstadoEnvio estado) {
        Envio envio = new Envio();
        envio.setId(1L);
        envio.setEstado(estado);
        return envio;
    }

    private Usuario usuario(Long id, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setRol(rol);
        return u;
    }

    @Test
    void repartidorAsignadoPuedeIniciarEntrega() {
        Usuario repartidor = usuario(10L, Rol.REPARTIDOR);
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envioEn(EstadoEnvio.ASIGNADO)));
        when(asignacionRepository.existsByEnvioIdAndRepartidorIdAndActivaTrue(1L, 10L)).thenReturn(true);

        Envio resultado = estadoService.cambiarEstado(1L, EstadoEnvio.EN_RUTA, repartidor, "Inicio");

        assertEquals(EstadoEnvio.EN_RUTA, resultado.getEstado());
        verify(historialRepository).save(any(HistorialEstado.class));
    }

    @Test
    void adminPuedeAsignarEnvioRegistrado() {
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envioEn(EstadoEnvio.REGISTRADO)));

        Envio resultado = estadoService.cambiarEstado(1L, EstadoEnvio.ASIGNADO, usuario(1L, Rol.ADMIN), "");

        assertEquals(EstadoEnvio.ASIGNADO, resultado.getEstado());
    }

    @Test
    void noSePuedePasarDeRegistradoAEntregado() {
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envioEn(EstadoEnvio.REGISTRADO)));

        AppException e = assertThrows(AppException.class,
                () -> estadoService.cambiarEstado(1L, EstadoEnvio.ENTREGADO, usuario(10L, Rol.REPARTIDOR), ""));

        assertEquals("TRANSICION_INVALIDA", e.getCodigo());
        verify(historialRepository, never()).save(any());
    }

    @Test
    void clienteNoPuedeAsignar() {
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envioEn(EstadoEnvio.REGISTRADO)));

        AppException e = assertThrows(AppException.class,
                () -> estadoService.cambiarEstado(1L, EstadoEnvio.ASIGNADO, usuario(5L, Rol.CLIENTE), ""));

        assertEquals("PROHIBIDO", e.getCodigo());
    }

    @Test
    void repartidorNoPuedeMoverEnvioQueNoEsSuyo() {
        when(envioRepository.findById(1L)).thenReturn(Optional.of(envioEn(EstadoEnvio.ASIGNADO)));
        when(asignacionRepository.existsByEnvioIdAndRepartidorIdAndActivaTrue(1L, 99L)).thenReturn(false);

        AppException e = assertThrows(AppException.class,
                () -> estadoService.cambiarEstado(1L, EstadoEnvio.EN_RUTA, usuario(99L, Rol.REPARTIDOR), ""));

        assertEquals("PROHIBIDO", e.getCodigo());
    }

    @Test
    void envioInexistenteDevuelveNoEncontrado() {
        when(envioRepository.findById(1L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class,
                () -> estadoService.cambiarEstado(1L, EstadoEnvio.EN_RUTA, usuario(10L, Rol.REPARTIDOR), ""));

        assertEquals("NO_ENCONTRADO", e.getCodigo());
    }

    @Test
    void reasignacionDejaEstadoAsignadoYRegistraHistorial() {
        Envio envio = envioEn(EstadoEnvio.ASIGNADO);

        estadoService.registrarReasignacion(envio, usuario(1L, Rol.ADMIN), "Repartidor enfermo");

        assertEquals(EstadoEnvio.ASIGNADO, envio.getEstado());
        verify(historialRepository).save(any(HistorialEstado.class));
    }

    @Test
    void noSePuedeReasignarEnvioEnRuta() {
        AppException e = assertThrows(AppException.class,
                () -> estadoService.registrarReasignacion(envioEn(EstadoEnvio.EN_RUTA), usuario(1L, Rol.ADMIN), ""));

        assertEquals("TRANSICION_INVALIDA", e.getCodigo());
    }
}
