package co.edu.uq.uqdelivery.repository;

import co.edu.uq.uqdelivery.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {
    Optional<Asignacion> findByEnvioIdAndActivaTrue(Long envioId);
    boolean existsByEnvioIdAndRepartidorIdAndActivaTrue(Long envioId, Long repartidorId);
    List<Asignacion> findByRepartidorIdAndActivaTrue(Long repartidorId);
}
