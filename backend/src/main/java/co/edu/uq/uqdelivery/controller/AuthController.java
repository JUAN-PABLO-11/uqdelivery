package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.request.LoginRequest;
import co.edu.uq.uqdelivery.dto.request.RegistroRequest;
import co.edu.uq.uqdelivery.dto.response.LoginResponse;
import co.edu.uq.uqdelivery.dto.response.UsuarioResponse;
import co.edu.uq.uqdelivery.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** HU-01: endpoints públicos de autenticación. */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 201 creado · 400 validación · 409 correo repetido */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registro(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    /** 200 token · 400 validación · 401 credenciales inválidas · 423 cuenta bloqueada */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
