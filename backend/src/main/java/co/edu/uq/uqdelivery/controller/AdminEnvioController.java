package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.AsignarRequest;
import co.edu.uq.uqdelivery.dto.ReasignarRequest;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.service.AsignacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/envios")
public class AdminEnvioController {

    private final AsignacionService asignacionService;

    public AdminEnvioController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @PostMapping("/{id}/asignar")
    public ResponseEntity<Void> asignar(@PathVariable Long id,
                                        @Valid @RequestBody AsignarRequest request,
                                        @AuthenticationPrincipal Usuario admin) {
        asignacionService.asignarEnvio(id, request, admin);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reasignar")
    public ResponseEntity<Void> reasignar(@PathVariable Long id,
                                          @Valid @RequestBody ReasignarRequest request,
                                          @AuthenticationPrincipal Usuario admin) {
        asignacionService.reasignarEnvio(id, request, admin);
        return ResponseEntity.ok().build();
    }
}