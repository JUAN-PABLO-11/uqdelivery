package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.response.EntregaResponse;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.service.EntregaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/repartidor")
@RequiredArgsConstructor
public class RepartidorController {

    private final EntregaService entregaService;

    // ÚNICO ENDPOINT: Listar las entregas del repartidor autenticado
    @GetMapping("/entregas")
    public ResponseEntity<List<EntregaResponse>> obtenerEntregas(@AuthenticationPrincipal Usuario repartidor) {
        return ResponseEntity.ok(entregaService.obtenerMisEntregas(repartidor));
    }
}