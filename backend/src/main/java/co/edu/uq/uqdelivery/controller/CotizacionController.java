package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.request.CotizacionRequest;
import co.edu.uq.uqdelivery.dto.response.CotizacionResponse;
import co.edu.uq.uqdelivery.service.CotizacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint de HU-05. La cotización requiere una sesión de cliente y no persiste un envío. */
@RestController
@RequestMapping("/api/v1/envios")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;

    @PostMapping("/cotizar")
    @PreAuthorize("hasRole('CLIENTE')")
    public CotizacionResponse cotizar(@Valid @RequestBody CotizacionRequest request) {
        return cotizacionService.cotizar(request);
    }
}