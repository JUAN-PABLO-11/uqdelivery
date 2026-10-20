package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.dto.request.LoginRequest;
import co.edu.uq.uqdelivery.dto.response.LoginResponse;
import co.edu.uq.uqdelivery.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // TODO HU-01: POST /registro
}
