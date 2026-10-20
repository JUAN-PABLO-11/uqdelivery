package co.edu.uq.uqdelivery.repository;

import co.edu.uq.uqdelivery.model.Rol;
import co.edu.uq.uqdelivery.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    List<Usuario> findByRolAndActivoTrue(Rol rol);
}
