package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.ActualizarUsuarioRequest;
import co.edu.uq.uqdelivery.dto.request.CambiarRolRequest;
import co.edu.uq.uqdelivery.dto.request.CrearUsuarioRequest;
import co.edu.uq.uqdelivery.dto.response.UsuarioResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.Rol;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** HU-02: gestión de perfiles y roles. */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private UsuarioService usuarioService;

    private final Usuario admin = usuario(1L, "Admin", "admin@uq.com", Rol.ADMIN);

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, encoder);
    }

    private static Usuario usuario(Long id, String nombre, String correo, Rol rol) {
        Usuario u = new Usuario(nombre, correo, "hash", rol);
        u.setId(id);
        return u;
    }

    private void devolverLoGuardado() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void crearUsuarioGuardaLaPasswordCifradaYElRolElegido() {
        when(usuarioRepository.existsByCorreo("rep@uq.com")).thenReturn(false);
        devolverLoGuardado();

        UsuarioResponse resp = usuarioService.crear(
                new CrearUsuarioRequest(" Repartidor ", " Rep@UQ.com ", "Segura123*", Rol.REPARTIDOR));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("rep@uq.com", captor.getValue().getCorreo());
        assertTrue(encoder.matches("Segura123*", captor.getValue().getPasswordHash()));
        assertEquals(Rol.REPARTIDOR, resp.rol());
        assertTrue(resp.activo());
    }

    @Test
    void crearConCorreoRepetidoDevuelveConflicto() {
        when(usuarioRepository.existsByCorreo("rep@uq.com")).thenReturn(true);

        AppException e = assertThrows(AppException.class, () -> usuarioService.crear(
                new CrearUsuarioRequest("Rep", "rep@uq.com", "Segura123*", Rol.REPARTIDOR)));

        assertEquals(HttpStatus.CONFLICT, e.getEstado());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void listarFiltraPorRol() {
        when(usuarioRepository.findAll(any(Sort.class))).thenReturn(List.of(
                admin,
                usuario(2L, "Ana", "ana@uq.com", Rol.CLIENTE),
                usuario(3L, "Beto", "beto@uq.com", Rol.REPARTIDOR)));

        List<UsuarioResponse> repartidores = usuarioService.listar(Rol.REPARTIDOR);
        List<UsuarioResponse> todos = usuarioService.listar(null);

        assertEquals(1, repartidores.size());
        assertEquals("Beto", repartidores.get(0).nombre());
        assertEquals(3, todos.size());
    }

    @Test
    void cambiarRolDeClienteARepartidor() {
        Usuario cliente = usuario(2L, "Ana", "ana@uq.com", Rol.CLIENTE);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(cliente));
        devolverLoGuardado();

        UsuarioResponse resp = usuarioService.cambiarRol(2L, new CambiarRolRequest(Rol.REPARTIDOR), admin);

        assertEquals(Rol.REPARTIDOR, resp.rol());
    }

    @Test
    void elAdminNoPuedeCambiarSuPropioRol() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        AppException e = assertThrows(AppException.class,
                () -> usuarioService.cambiarRol(1L, new CambiarRolRequest(Rol.CLIENTE), admin));

        assertEquals("OPERACION_NO_PERMITIDA", e.getCodigo());
    }

    @Test
    void actualizarConCorreoDeOtroUsuarioDevuelveConflicto() {
        Usuario ana = usuario(2L, "Ana", "ana@uq.com", Rol.CLIENTE);
        Usuario beto = usuario(3L, "Beto", "beto@uq.com", Rol.CLIENTE);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(ana));
        when(usuarioRepository.findByCorreo("beto@uq.com")).thenReturn(Optional.of(beto));

        AppException e = assertThrows(AppException.class,
                () -> usuarioService.actualizar(2L, new ActualizarUsuarioRequest("Ana", "beto@uq.com")));

        assertEquals("CORREO_DUPLICADO", e.getCodigo());
    }

    @Test
    void desactivarUsuario() {
        Usuario ana = usuario(2L, "Ana", "ana@uq.com", Rol.CLIENTE);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(ana));
        devolverLoGuardado();

        UsuarioResponse resp = usuarioService.cambiarEstado(2L, false, admin);

        assertFalse(resp.activo());
    }

    @Test
    void reactivarUsuarioLimpiaElBloqueoPorIntentos() {
        Usuario ana = usuario(2L, "Ana", "ana@uq.com", Rol.CLIENTE);
        ana.setActivo(false);
        ana.setIntentosFallidos(3);
        ana.setBloqueadoHasta(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(ana));
        devolverLoGuardado();

        UsuarioResponse resp = usuarioService.cambiarEstado(2L, true, admin);

        assertTrue(resp.activo());
        assertEquals(0, ana.getIntentosFallidos());
        assertNull(ana.getBloqueadoHasta());
    }

    @Test
    void elAdminNoPuedeDesactivarseASiMismo() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        AppException e = assertThrows(AppException.class, () -> usuarioService.desactivar(1L, admin));

        assertEquals("OPERACION_NO_PERMITIDA", e.getCodigo());
        assertTrue(admin.isActivo());
    }

    @Test
    void usuarioInexistenteDevuelveNoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> usuarioService.cambiarEstado(99L, false, admin));

        assertEquals(HttpStatus.NOT_FOUND, e.getEstado());
    }
}
