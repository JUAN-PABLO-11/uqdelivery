package co.edu.uq.uqdelivery.security;

import co.edu.uq.uqdelivery.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiracionMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expiracionMinutos) {
        // HS256 exige un secreto de al menos 32 caracteres
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMinutos * 60_000;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(key)
                .compact();
    }

    /** Devuelve el id del usuario. Lanza JwtException si el token es inválido o expiró. */
    public Long extraerUsuarioId(String token) {
        String subject = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
        return Long.valueOf(subject);
    }
}
