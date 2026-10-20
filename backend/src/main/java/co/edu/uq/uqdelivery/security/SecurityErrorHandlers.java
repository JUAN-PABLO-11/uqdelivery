package co.edu.uq.uqdelivery.security;

import co.edu.uq.uqdelivery.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Hace que los 401 y 403 de Spring Security usen el mismo formato de error que el resto de la API. */
@Component
@RequiredArgsConstructor
public class SecurityErrorHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e) throws IOException {
        escribir(res, HttpServletResponse.SC_UNAUTHORIZED, new ErrorResponse("NO_AUTENTICADO", "Debe iniciar sesión"));
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException e) throws IOException {
        escribir(res, HttpServletResponse.SC_FORBIDDEN, new ErrorResponse("PROHIBIDO", "Acceso no autorizado"));
    }

    private void escribir(HttpServletResponse res, int status, ErrorResponse body) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(res.getWriter(), body);
    }
}
