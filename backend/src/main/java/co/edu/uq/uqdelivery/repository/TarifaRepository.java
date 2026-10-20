package co.edu.uq.uqdelivery.repository;

import co.edu.uq.uqdelivery.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    Optional<Tarifa> findFirstByVigenteTrue();
}
