package co.edu.uq.uqdelivery.repository;

import co.edu.uq.uqdelivery.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    List<HistorialEstado> findByEnvioIdOrderByCreadoEnAsc(Long envioId);
}
