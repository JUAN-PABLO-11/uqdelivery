package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.request.AsignarRequest;
import co.edu.uq.uqdelivery.dto.request.ReasignarRequest;
import co.edu.uq.uqdelivery.dto.response.AdminEnvioResponse;
import co.edu.uq.uqdelivery.dto.response.RepartidorResponse;
import co.edu.uq.uqdelivery.model.EstadoEnvio;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.service.AsignacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HU-06: el administrador consulta envíos y repartidores, y asigna o reasigna. Rol ADMIN (SecurityConfig). */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminEnvioController {

    private final AsignacionService asignacionService;

    /** Todos los envíos, del más reciente al más antiguo. Filtro opcional: ?estado=REGISTRADO */
    @GetMapping("/envios")
    public List<AdminEnvioResponse> listarEnvios(@RequestParam(required = false) EstadoEnvio estado) {
        return asignacionService.listarEnviosAdmin(estado);
    }

    /** Repartidores activos y disponibles para asignar. */
    @GetMapping("/repartidores")
    public List<RepartidorResponse> listarRepartidores() {
        return asignacionService.listarRepartidoresDisponibles();
    }

    @PostMapping("/envios/{id}/asignar")
    public ResponseEntity<Void> asignar(@PathVariable Long id,
                                        @Valid @RequestBody AsignarRequest request,
                                        @AuthenticationPrincipal Usuario admin) {
        asignacionService.asignarEnvio(id, request, admin);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/envios/{id}/reasignar")
    public ResponseEntity<Void> reasignar(@PathVariable Long id,
                                          @Valid @RequestBody ReasignarRequest request,
                                          @AuthenticationPrincipal Usuario admin) {
        asignacionService.reasignarEnvio(id, request, admin);
        return ResponseEntity.ok().build();
    }
}
