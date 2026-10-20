package co.edu.uq.uqdelivery.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El correo es obligatorio") @Email(message = "Correo inválido") String correo,
        @NotBlank(message = "La contraseña es obligatoria") String password) {
}
