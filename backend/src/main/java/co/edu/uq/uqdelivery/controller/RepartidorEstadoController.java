package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.request.CambiarEstadoRequest;
import co.edu.uq.uqdelivery.dto.response.EnvioEstadoResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.EstadoEnvio;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.service.EstadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * Caso 00: "Iniciar entrega" / "Retomar" (EN_RUTA) y "Marcar entregado" (ENTREGADO).
 * CON_NOVEDAD NO va por aquí: se hace al reportar la novedad (HU-10).
 */
@RestController
@RequestMapping("/api/v1/repartidor/envios")
@RequiredArgsConstructor
public class RepartidorEstadoController {

    private static final Set<EstadoEnvio> PERMITIDOS = Set.of(EstadoEnvio.EN_RUTA, EstadoEnvio.ENTREGADO);

    private final EstadoService estadoService;

    @PatchMapping("/{id}/estado")
    public EnvioEstadoResponse cambiarEstado(@PathVariable Long id,
                                             @Valid @RequestBody CambiarEstadoRequest request,
                                             @AuthenticationPrincipal Usuario actor) {
        if (!PERMITIDOS.contains(request.estado())) {
            throw new AppException("VALIDACION", "Estado no permitido en este endpoint", HttpStatus.BAD_REQUEST);
        }
        return EnvioEstadoResponse.de(estadoService.cambiarEstado(id, request.estado(), actor, request.detalle()));
    }
}
