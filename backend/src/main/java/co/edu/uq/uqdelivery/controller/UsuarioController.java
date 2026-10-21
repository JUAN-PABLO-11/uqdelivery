package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.request.ActualizarUsuarioRequest;
import co.edu.uq.uqdelivery.dto.request.CambiarRolRequest;
import co.edu.uq.uqdelivery.dto.request.CrearUsuarioRequest;
import co.edu.uq.uqdelivery.dto.response.UsuarioResponse;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(
            @Valid @RequestBody CrearUsuarioRequest request
    ) {
        return usuarioService.crear(request);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request
    ) {
        return usuarioService.actualizar(id, request);
    }

    @PatchMapping("/{id}/rol")
    public UsuarioResponse cambiarRol(
            @PathVariable Long id,
            @Valid @RequestBody CambiarRolRequest request,
            @AuthenticationPrincipal Usuario actor
    ) {
        return usuarioService.cambiarRol(id, request, actor);
    }

    @PatchMapping("/{id}/desactivar")
    public UsuarioResponse desactivar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario actor
    ) {
        return usuarioService.desactivar(id, actor);
    }
}