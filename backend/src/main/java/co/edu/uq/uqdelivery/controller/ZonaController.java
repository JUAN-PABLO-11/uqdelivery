package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.response.ZonaResponse;
import co.edu.uq.uqdelivery.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Zonas activas para los selects de origen/destino (HU-04 y HU-05). */
@RestController
@RequestMapping("/api/v1/zonas")
@RequiredArgsConstructor
public class ZonaController {

    private final ZonaRepository zonaRepository;

    @GetMapping
    public List<ZonaResponse> listar() {
        return zonaRepository.findByActivaTrueOrderByNombre().stream().map(ZonaResponse::de).toList();
    }
}
