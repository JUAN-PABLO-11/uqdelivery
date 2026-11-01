package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.CotizacionRequest;
import co.edu.uq.uqdelivery.dto.request.CrearEnvioRequest;
import co.edu.uq.uqdelivery.dto.response.CotizacionResponse;
import co.edu.uq.uqdelivery.dto.response.EnvioResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.*;
import co.edu.uq.uqdelivery.repository.EnvioRepository;
import co.edu.uq.uqdelivery.repository.ZonaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock EnvioRepository envioRepository;
    @Mock ZonaRepository zonaRepository;
    @Mock CotizacionService cotizacionService;
    @Mock EstadoService estadoService;
    @InjectMocks EnvioService envioService;

    // ---------- datos de prueba ----------

    private Usuario cliente(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setRol(Rol.CLIENTE);
        return u;
    }

    private Zona zona(Long id, String nombre) {
        Zona z = new Zona(nombre, 4.53, -75.68, true);
        z.setId(id);
        return z;
    }

    private CrearEnvioRequest request() {
        return new CrearEnvioRequest("  Juan Pérez ", "María Gómez", "Cra 14 #10-20", "Cll 5 #3-40",
                1L, 3L, 2.5, 20, 30, 40);
    }

    private CotizacionResponse cotizacion() {
        return new CotizacionResponse(4.37, new BigDecimal("5000.00"), new BigDecimal("5244.00"),
                new BigDecimal("2000.00"), new BigDecimal("12244.00"));
    }

    private Envio envioDe(Long id, Usuario dueno) {
        Envio e = new Envio();
        e.setId(id);
        e.setCodigo(String.format("UQD-%06d", id));
        e.setCliente(dueno);
        e.setZonaOrigen(zona(1L, "Centro"));
        e.setZonaDestino(zona(3L, "Sur"));
        return e;
    }

    /** Simula la BD: al guardar le asigna el id 123. */
    private void bdAsignaId123() {
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> {
            Envio e = inv.getArgument(0);
            e.setId(123L);
            return e;
        });
    }

    // ---------- crear ----------

    @Test
    void creaElEnvioRegistradoConCodigoDeSeguimiento() {
        Usuario cliente = cliente(10L);
        when(cotizacionService.cotizar(any())).thenReturn(cotizacion());
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona(1L, "Centro")));
        when(zonaRepository.findById(3L)).thenReturn(Optional.of(zona(3L, "Sur")));
        bdAsignaId123();

        EnvioResponse r = envioService.crear(request(), cliente);

        assertEquals(123L, r.id());
        assertEquals("UQD-000123", r.codigo());
        assertEquals(EstadoEnvio.REGISTRADO, r.estado());
        assertEquals("Centro", r.zonaOrigen());
        assertEquals("Sur", r.zonaDestino());
        assertEquals("Juan Pérez", r.remitente());          // se quitan los espacios de más
    }

    @Test
    void elValorYLaDistanciaSalenDeLaCotizacion() {
        when(cotizacionService.cotizar(any())).thenReturn(cotizacion());
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona(1L, "Centro")));
        when(zonaRepository.findById(3L)).thenReturn(Optional.of(zona(3L, "Sur")));
        bdAsignaId123();

        EnvioResponse r = envioService.crear(request(), cliente(10L));

        assertEquals(new BigDecimal("12244.00"), r.valorTotal());
        assertEquals(4.37, r.distanciaKm());
        verify(cotizacionService).cotizar(new CotizacionRequest(1L, 3L, 2.5, 20, 30, 40));
    }

    @Test
    void elClienteDelEnvioEsElUsuarioAutenticado() {
        Usuario cliente = cliente(10L);
        when(cotizacionService.cotizar(any())).thenReturn(cotizacion());
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona(1L, "Centro")));
        when(zonaRepository.findById(3L)).thenReturn(Optional.of(zona(3L, "Sur")));
        bdAsignaId123();

        envioService.crear(request(), cliente);

        ArgumentCaptor<Envio> guardado = ArgumentCaptor.forClass(Envio.class);
        verify(envioRepository).save(guardado.capture());
        assertSame(cliente, guardado.getValue().getCliente());
    }

    @Test
    void escribeElHistorialDeCreacion() {
        Usuario cliente = cliente(10L);
        when(cotizacionService.cotizar(any())).thenReturn(cotizacion());
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona(1L, "Centro")));
        when(zonaRepository.findById(3L)).thenReturn(Optional.of(zona(3L, "Sur")));
        bdAsignaId123();

        envioService.crear(request(), cliente);

        ArgumentCaptor<Envio> envio = ArgumentCaptor.forClass(Envio.class);
        verify(estadoService).registrarCreacion(envio.capture(), eq(cliente));
        assertEquals("UQD-000123", envio.getValue().getCodigo());
    }

    @Test
    void destinoFueraDeCoberturaNoGuardaNada() {
        when(cotizacionService.cotizar(any())).thenThrow(
                new AppException("FUERA_DE_COBERTURA", "Destino fuera de cobertura", HttpStatus.UNPROCESSABLE_ENTITY));

        AppException e = assertThrows(AppException.class, () -> envioService.crear(request(), cliente(10L)));

        assertEquals("FUERA_DE_COBERTURA", e.getCodigo());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.getEstado());
        verify(envioRepository, never()).save(any());
        verify(estadoService, never()).registrarCreacion(any(), any());
    }

    // ---------- listar ----------

    @Test
    void listaSoloLosEnviosDelCliente() {
        Usuario cliente = cliente(10L);
        when(envioRepository.findByClienteIdOrderByCreadoEnDesc(10L))
                .thenReturn(List.of(envioDe(2L, cliente), envioDe(1L, cliente)));

        List<EnvioResponse> r = envioService.listarMios(cliente);

        assertEquals(List.of("UQD-000002", "UQD-000001"), r.stream().map(EnvioResponse::codigo).toList());
    }

    // ---------- detalle ----------

    @Test
    void elClienteVeElDetalleDeSuEnvio() {
        Usuario cliente = cliente(10L);
        when(envioRepository.findById(5L)).thenReturn(Optional.of(envioDe(5L, cliente)));

        EnvioResponse r = envioService.detalle(5L, cliente);

        assertEquals("UQD-000005", r.codigo());
    }

    @Test
    void unClienteNoPuedeVerElEnvioDeOtro() {
        when(envioRepository.findById(5L)).thenReturn(Optional.of(envioDe(5L, cliente(10L))));

        AppException e = assertThrows(AppException.class, () -> envioService.detalle(5L, cliente(20L)));

        assertEquals(HttpStatus.FORBIDDEN, e.getEstado());
    }

    @Test
    void envioInexistenteResponde404() {
        when(envioRepository.findById(99L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> envioService.detalle(99L, cliente(10L)));

        assertEquals(HttpStatus.NOT_FOUND, e.getEstado());
    }
}
