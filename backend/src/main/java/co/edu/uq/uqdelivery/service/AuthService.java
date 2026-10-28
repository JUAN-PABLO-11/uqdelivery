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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * HU-01: Registro e inicio de sesión seguro.
 *
 * Criterio 1: registro con correo no registrado y contraseña segura (8+ caracteres, mayúscula,
 *             número y símbolo). La contraseña se guarda con hash BCrypt.
 * Criterio 2: tras 5 intentos fallidos seguidos, la cuenta queda bloqueada 15 minutos
 *             (HTTP 423 "Cuenta bloqueada temporalmente"). Con credenciales válidas se emite un JWT.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    public static final int MAX_INTENTOS = 5;
    public static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Clock clock;

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        String correo = normalizarCorreo(request.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new AppException("CORREO_DUPLICADO", "Ya existe una cuenta con ese correo", HttpStatus.CONFLICT);
        }

        Usuario usuario = new Usuario(
                request.nombre().trim(),
                correo,
                passwordEncoder.encode(request.password()),
                Rol.CLIENTE);

        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    /**
     * noRollbackFor: si la contraseña es incorrecta se lanza una AppException, pero el contador
     * de intentos fallidos y el bloqueo SÍ deben quedar guardados en la BD.
     */
    @Transactional(noRollbackFor = AppException.class)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(normalizarCorreo(request.correo()))
                .filter(Usuario::isActivo)
                .orElseThrow(this::credencialesInvalidas);

        LocalDateTime ahora = LocalDateTime.now(clock);

        if (estaBloqueado(usuario, ahora)) {
            throw cuentaBloqueada(usuario, ahora);
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            registrarIntentoFallido(usuario, ahora);
            throw credencialesInvalidas();
        }

        // Login correcto: se reinicia el contador
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        return new LoginResponse(jwtService.generarToken(usuario), usuario.getId(), usuario.getNombre(),
                usuario.getCorreo(), usuario.getRol());
    }

    private boolean estaBloqueado(Usuario usuario, LocalDateTime ahora) {
        return usuario.getBloqueadoHasta() != null && ahora.isBefore(usuario.getBloqueadoHasta());
    }

    private void registrarIntentoFallido(Usuario usuario, LocalDateTime ahora) {
        // Si venía de un bloqueo ya vencido, empieza a contar de nuevo
        if (usuario.getBloqueadoHasta() != null) {
            usuario.setBloqueadoHasta(null);
            usuario.setIntentosFallidos(0);
        }

        int intentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(intentos);

        if (intentos >= MAX_INTENTOS) {
            usuario.setBloqueadoHasta(ahora.plus(DURACION_BLOQUEO));
            usuario.setIntentosFallidos(0);
        }
        usuarioRepository.save(usuario);
    }

    private AppException cuentaBloqueada(Usuario usuario, LocalDateTime ahora) {
        long segundos = Duration.between(ahora, usuario.getBloqueadoHasta()).getSeconds();
        long minutos = Math.max(1, (segundos + 59) / 60);   // redondea hacia arriba
        return new AppException("CUENTA_BLOQUEADA",
                "Cuenta bloqueada temporalmente. Intenta de nuevo en " + minutos + " minuto(s)",
                HttpStatus.LOCKED);
    }

    /** Mismo mensaje para correo inexistente y contraseña errada: no revela qué correos existen. */
    private AppException credencialesInvalidas() {
        return new AppException("CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }
}
