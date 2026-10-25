package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.request.CrearEnvioRequest;
import co.edu.uq.uqdelivery.dto.response.EnvioResponse;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.service.EnvioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HU-04: el cliente crea y consulta sus envíos.
 * La cotización (POST /envios/cotizar) está en CotizacionController (HU-05).
 */
@RestController
@RequestMapping("/api/v1/envios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENTE')")
public class EnvioController {

    private final EnvioService envioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnvioResponse crear(@Valid @RequestBody CrearEnvioRequest request,
                               @AuthenticationPrincipal Usuario cliente) {
        return envioService.crear(request, cliente);
    }

    @GetMapping
    public List<EnvioResponse> listarMios(@AuthenticationPrincipal Usuario cliente) {
        return envioService.listarMios(cliente);
    }

    @GetMapping("/{id}")
    public EnvioResponse detalle(@PathVariable Long id, @AuthenticationPrincipal Usuario cliente) {
        return envioService.detalle(id, cliente);
    }
}
