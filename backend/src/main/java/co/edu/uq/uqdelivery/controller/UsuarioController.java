package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.request.ActualizarUsuarioRequest;
import co.edu.uq.uqdelivery.dto.request.CambiarEstadoUsuarioRequest;
import co.edu.uq.uqdelivery.dto.request.CambiarRolRequest;
import co.edu.uq.uqdelivery.dto.request.CrearUsuarioRequest;
import co.edu.uq.uqdelivery.dto.response.UsuarioResponse;
import co.edu.uq.uqdelivery.model.Rol;
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

    /** Filtro opcional: /admin/usuarios?rol=REPARTIDOR */
    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) Rol rol) {
        return usuarioService.listar(rol);
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

    /** Activa o desactiva la cuenta: { "activo": true | false } */
    @PatchMapping("/{id}/estado")
    public UsuarioResponse cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoUsuarioRequest request,
            @AuthenticationPrincipal Usuario actor
    ) {
        return usuarioService.cambiarEstado(id, request.activo(), actor);
    }

    @PatchMapping("/{id}/desactivar")
    public UsuarioResponse desactivar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario actor
    ) {
        return usuarioService.desactivar(id, actor);
    }
}