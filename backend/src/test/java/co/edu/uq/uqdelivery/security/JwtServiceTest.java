package co.edu.uq.uqdelivery.security;

import co.edu.uq.uqdelivery.model.Rol;
import co.edu.uq.uqdelivery.model.Usuario;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService("secreto-de-pruebas-de-al-menos-32-caracteres-123", 60);

    @Test
    void generaYLeeElIdDelUsuario() {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setRol(Rol.CLIENTE);

        String token = jwtService.generarToken(u);

        assertEquals(7L, jwtService.extraerUsuarioId(token));
    }

    @Test
    void rechazaTokenAlterado() {
        assertThrows(JwtException.class, () -> jwtService.extraerUsuarioId("token.falso.123"));
    }
}
