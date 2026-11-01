package co.edu.uq.uqdelivery.dto.request;

import co.edu.uq.uqdelivery.validation.SinHtml;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * HU-01: registro público. No incluye el rol: toda cuenta creada por registro es CLIENTE.
 * Los repartidores y administradores los crea el ADMIN (HU-02).
 */
public record RegistroRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        @SinHtml
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo inválido")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "La contraseña debe tener mayúscula, número y símbolo"
        )
        String password
) {
}
