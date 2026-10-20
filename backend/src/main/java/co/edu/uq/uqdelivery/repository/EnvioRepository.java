package co.edu.uq.uqdelivery.repository;

import co.edu.uq.uqdelivery.model.Envio;
import co.edu.uq.uqdelivery.model.EstadoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Long> {
    List<Envio> findByClienteIdOrderByCreadoEnDesc(Long clienteId);
    List<Envio> findByEstadoOrderByCreadoEnDesc(EstadoEnvio estado);
    Optional<Envio> findByCodigo(String codigo);
}
