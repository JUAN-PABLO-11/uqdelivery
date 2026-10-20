package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.LoginRequest;
import co.edu.uq.uqdelivery.dto.response.LoginResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.repository.UsuarioRepository;
import co.edu.uq.uqdelivery.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Login BÁSICO del Caso 00 para que todos puedan obtener un token desde el día 1.
 * HU-01 lo completa: registro, bloqueo tras 5 intentos (423), validación de contraseña segura.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.correo().trim().toLowerCase())
                .filter(Usuario::isActivo)
                .orElseThrow(this::credencialesInvalidas);

        // TODO HU-01: verificar bloqueadoHasta (423) y contar intentosFallidos
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw credencialesInvalidas();
        }

        return new LoginResponse(jwtService.generarToken(usuario), usuario.getId(), usuario.getNombre(),
                usuario.getCorreo(), usuario.getRol());
    }

    private AppException credencialesInvalidas() {
        return new AppException("CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos", HttpStatus.UNAUTHORIZED);
    }
}
