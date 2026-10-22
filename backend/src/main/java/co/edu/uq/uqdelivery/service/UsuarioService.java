package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.ActualizarUsuarioRequest;
import co.edu.uq.uqdelivery.dto.request.CambiarRolRequest;
import co.edu.uq.uqdelivery.dto.request.CrearUsuarioRequest;
import co.edu.uq.uqdelivery.dto.response.UsuarioResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest request) {
        String correo = normalizarCorreo(request.correo());

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new AppException(
                    "CORREO_DUPLICADO",
                    "Ya existe un usuario con ese correo",
                    HttpStatus.CONFLICT
            );
        }

        Usuario usuario = new Usuario(
                request.nombre().trim(),
                correo,
                passwordEncoder.encode(request.password()),
                request.rol()
        );

        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);

        return UsuarioResponse.de(guardado);
    }

    @Transactional
    public UsuarioResponse actualizar(
            Long id,
            ActualizarUsuarioRequest request
    ) {
        Usuario usuario = buscar(id);
        String correo = normalizarCorreo(request.correo());

        usuarioRepository.findByCorreo(correo)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new AppException(
                            "CORREO_DUPLICADO",
                            "Ya existe un usuario con ese correo",
                            HttpStatus.CONFLICT
                    );
                });

        usuario.setNombre(request.nombre().trim());
        usuario.setCorreo(correo);

        return UsuarioResponse.de(
                usuarioRepository.save(usuario)
        );
    }

    @Transactional
    public UsuarioResponse cambiarRol(
            Long id,
            CambiarRolRequest request,
            Usuario actor
    ) {
        Usuario usuario = buscar(id);

        // Evita que un administrador cambie su propio rol.
        if (usuario.getId().equals(actor.getId())
                && usuario.getRol() != request.rol()) {

            throw new AppException(
                    "OPERACION_NO_PERMITIDA",
                    "No puedes cambiar tu propio rol",
                    HttpStatus.CONFLICT
            );
        }

        usuario.setRol(request.rol());

        return UsuarioResponse.de(
                usuarioRepository.save(usuario)
        );
    }

    @Transactional
    public UsuarioResponse desactivar(Long id, Usuario actor) {
        Usuario usuario = buscar(id);

        // Evita que el administrador se desactive a sí mismo.
        if (usuario.getId().equals(actor.getId())) {
            throw new AppException(
                    "OPERACION_NO_PERMITIDA",
                    "No puedes desactivar tu propia cuenta",
                    HttpStatus.CONFLICT
            );
        }

        usuario.setActivo(false);

        return UsuarioResponse.de(
                usuarioRepository.save(usuario)
        );
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        AppException.noEncontrado(
                                "No existe el usuario solicitado"
                        )
                );
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }
}