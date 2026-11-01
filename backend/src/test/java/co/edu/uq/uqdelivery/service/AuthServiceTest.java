package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.LoginRequest;
import co.edu.uq.uqdelivery.dto.request.RegistroRequest;
import co.edu.uq.uqdelivery.dto.response.LoginResponse;
import co.edu.uq.uqdelivery.dto.response.UsuarioResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.Rol;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.repository.UsuarioRepository;
import co.edu.uq.uqdelivery.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String PASSWORD = "Segura123*";
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Instant INSTANTE = Instant.parse("2026-10-27T15:00:00Z");

    @Mock UsuarioRepository usuarioRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4); // costo bajo: pruebas rápidas
    private final JwtService jwtService = new JwtService("secreto-de-pruebas-de-al-menos-32-caracteres-123", 60);
    private final Clock clock = Clock.fixed(INSTANTE, ZONA);
    private final LocalDateTime ahora = LocalDateTime.now(clock);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, encoder, jwtService, clock);
    }

    private Usuario cliente() {
        Usuario u = new Usuario("Laura", "laura@correo.com", encoder.encode(PASSWORD), Rol.CLIENTE);
        u.setId(1L);
        return u;
    }

    private AppException loginFallido(String password) {
        return assertThrows(AppException.class,
                () -> authService.login(new LoginRequest("laura@correo.com", password)));
    }

    // ================= Registro (criterio 1) =================

    @Test
    void registroCreaClienteConPasswordCifrada() {
        when(usuarioRepository.existsByCorreo("nuevo@correo.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        UsuarioResponse resp = authService.registrar(
                new RegistroRequest("  Nuevo Cliente ", "  Nuevo@Correo.com ", PASSWORD));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertEquals(10L, resp.id());
        assertEquals("nuevo@correo.com", guardado.getCorreo());       // correo normalizado
        assertEquals("Nuevo Cliente", guardado.getNombre());          // nombre sin espacios extra
        assertEquals(Rol.CLIENTE, guardado.getRol());                 // registro público = CLIENTE
        assertNotEquals(PASSWORD, guardado.getPasswordHash());        // nunca en texto plano
        assertTrue(encoder.matches(PASSWORD, guardado.getPasswordHash()));
    }

    @Test
    void registroConCorreoExistenteDevuelveConflicto() {
        when(usuarioRepository.existsByCorreo("laura@correo.com")).thenReturn(true);

        AppException e = assertThrows(AppException.class,
                () -> authService.registrar(new RegistroRequest("Laura", "laura@correo.com", PASSWORD)));

        assertEquals("CORREO_DUPLICADO", e.getCodigo());
        assertEquals(HttpStatus.CONFLICT, e.getEstado());
        verify(usuarioRepository, never()).save(any());
    }

    // ================= Login (criterio 2) =================

    @Test
    void loginCorrectoDevuelveTokenYReiniciaIntentos() {
        Usuario u = cliente();
        u.setIntentosFallidos(3);
        when(usuarioRepository.findByCorreo("laura@correo.com")).thenReturn(Optional.of(u));

        LoginResponse resp = authService.login(new LoginRequest("Laura@Correo.com", PASSWORD));

        assertNotNull(resp.token());
        assertEquals(1L, jwtService.extraerUsuarioId(resp.token()));
        assertEquals(Rol.CLIENTE, resp.rol());
        assertEquals(0, u.getIntentosFallidos());
    }

    @Test
    void passwordIncorrectaDevuelve401YSumaUnIntento() {
        Usuario u = cliente();
        when(usuarioRepository.findByCorreo("laura@correo.com")).thenReturn(Optional.of(u));

        AppException e = loginFallido("Incorrecta1*");

        assertEquals("CREDENCIALES_INVALIDAS", e.getCodigo());
        assertEquals(HttpStatus.UNAUTHORIZED, e.getEstado());
        assertEquals(1, u.getIntentosFallidos());
        assertNull(u.getBloqueadoHasta());
        verify(usuarioRepository).save(u);
    }

    @Test
    void elQuintoIntentoFallidoBloqueaLaCuenta15Minutos() {
        Usuario u = cliente();
        u.setIntentosFallidos(4);
        when(usuarioRepository.findByCorreo("laura@correo.com")).thenReturn(Optional.of(u));

        loginFallido("Incorrecta1*");

        assertEquals(ahora.plusMinutes(15), u.getBloqueadoHasta());
    }

    @Test
    void cuentaBloqueadaDevuelve423AunqueLaPasswordSeaCorrecta() {
        Usuario u = cliente();
        u.setBloqueadoHasta(ahora.plusMinutes(10));
        when(usuarioRepository.findByCorreo("laura@correo.com")).thenReturn(Optional.of(u));

        AppException e = loginFallido(PASSWORD);

        assertEquals("CUENTA_BLOQUEADA", e.getCodigo());
        assertEquals(HttpStatus.LOCKED, e.getEstado());
        assertTrue(e.getMessage().startsWith("Cuenta bloqueada temporalmente"));
    }

    @Test
    void seisIntentosSeguidosElSextoRecibeCuentaBloqueada() {
        Usuario u = cliente();
        when(usuarioRepository.findByCorreo("laura@correo.com")).thenReturn(Optional.of(u));

        for (int i = 1; i <= 5; i++) {
            assertEquals("CREDENCIALES_INVALIDAS", loginFallido("Incorrecta1*").getCodigo());
        }
        assertEquals("CUENTA_BLOQUEADA", loginFallido(PASSWORD).getCodigo());
    }

    @Test
    void cuandoVenceElBloqueoPuedeIniciarSesion() {
        Usuario u = cliente();
        u.setBloqueadoHasta(ahora.minusMinutes(1));
        when(usuarioRepository.findByCorreo("laura@correo.com")).thenReturn(Optional.of(u));

        LoginResponse resp = authService.login(new LoginRequest("laura@correo.com", PASSWORD));

        assertNotNull(resp.token());
        assertNull(u.getBloqueadoHasta());
    }

    @Test
    void trasVencerElBloqueoLosIntentosEmpiezanDeCero() {
        Usuario u = cliente();
        u.setBloqueadoHasta(ahora.minusMinutes(1));
        when(usuarioRepository.findByCorreo("laura@correo.com")).thenReturn(Optional.of(u));

        loginFallido("Incorrecta1*");

        assertEquals(1, u.getIntentosFallidos());
        assertNull(u.getBloqueadoHasta());
    }

    @Test
    void correoInexistenteDevuelveElMismoMensajeQuePasswordErrada() {
        when(usuarioRepository.findByCorreo("nadie@correo.com")).thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class,
                () -> authService.login(new LoginRequest("nadie@correo.com", PASSWORD)));

        assertEquals("CREDENCIALES_INVALIDAS", e.getCodigo());
        assertEquals("Correo o contraseña incorrectos", e.getMessage());
    }

    @Test
    void usuarioDesactivadoNoPuedeIniciarSesion() {
        Usuario u = cliente();
        u.setActivo(false);
        when(usuarioRepository.findByCorreo("laura@correo.com")).thenReturn(Optional.of(u));

        assertEquals("CREDENCIALES_INVALIDAS", loginFallido(PASSWORD).getCodigo());
    }
}
