package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.request.CrearNovedadRequest;
import co.edu.uq.uqdelivery.dto.response.NovedadResponse;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.service.NovedadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/repartidor/envios")
@RequiredArgsConstructor
public class NovedadController {

    private final NovedadService novedadService;

    @PostMapping("/{id}/novedades")
    public ResponseEntity<NovedadResponse> registrarNovedad(
            @PathVariable Long id,
            @Valid @RequestBody CrearNovedadRequest request,
            @AuthenticationPrincipal Usuario actor
    ) {
        NovedadResponse response = novedadService.registrarNovedad(id, request, actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}