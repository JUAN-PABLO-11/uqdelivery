package co.edu.uq.uqdelivery.dto.response;

import co.edu.uq.uqdelivery.model.Rol;

public record LoginResponse(String token, Long id, String nombre, String correo, Rol rol) {
}
