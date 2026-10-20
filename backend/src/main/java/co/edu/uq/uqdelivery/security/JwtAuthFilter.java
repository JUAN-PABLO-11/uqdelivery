package co.edu.uq.uqdelivery.security;

import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.repository.UsuarioRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee "Authorization: Bearer <token>", carga el usuario y lo deja como principal.
 * En los controllers se obtiene con:  @AuthenticationPrincipal Usuario actor
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Long id = jwtService.extraerUsuarioId(header.substring(7));
                usuarioRepository.findById(id).filter(Usuario::isActivo).ifPresent(usuario -> {
                    // OJO: hasRole("ADMIN") busca la autoridad "ROLE_ADMIN"
                    var authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());
                    var auth = new UsernamePasswordAuthenticationToken(usuario, null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (JwtException | IllegalArgumentException e) {
                // token inválido o vencido: la petición sigue sin autenticar y Spring responde 401
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
