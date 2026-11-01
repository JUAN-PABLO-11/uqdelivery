package co.edu.uq.uqdelivery.dto.request;

import co.edu.uq.uqdelivery.validation.SinHtml;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100,
                message = "El nombre no puede superar 100 caracteres")
        @SinHtml
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo inválido")
        @Size(max = 150,
                message = "El correo no puede superar 150 caracteres")
        String correo
) {
}