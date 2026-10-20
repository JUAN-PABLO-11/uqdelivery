package co.edu.uq.uqdelivery.repository;

import co.edu.uq.uqdelivery.model.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZonaRepository extends JpaRepository<Zona, Long> {
    List<Zona> findByActivaTrueOrderByNombre();
}
