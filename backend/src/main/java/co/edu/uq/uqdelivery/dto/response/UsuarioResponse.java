package co.edu.uq.uqdelivery.dto.response;

import co.edu.uq.uqdelivery.model.Rol;
import co.edu.uq.uqdelivery.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String correo,
        Rol rol,
        boolean activo
) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.isActivo()
        );
    }
}