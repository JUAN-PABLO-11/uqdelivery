package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.config.CorsConfig;
import co.edu.uq.uqdelivery.config.SecurityConfig;
import co.edu.uq.uqdelivery.dto.request.CrearEnvioRequest;
import co.edu.uq.uqdelivery.dto.response.EnvioResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.EstadoEnvio;
import co.edu.uq.uqdelivery.model.Rol;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.repository.UsuarioRepository;
import co.edu.uq.uqdelivery.security.JwtService;
import co.edu.uq.uqdelivery.security.SecurityErrorHandlers;
import co.edu.uq.uqdelivery.service.EnvioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HU-04: códigos HTTP y formato de error de /api/v1/envios, con la seguridad real del proyecto. */
@WebMvcTest(EnvioController.class)
@Import({SecurityConfig.class, SecurityErrorHandlers.class, CorsConfig.class})
class EnvioControllerTest {

    private static final String URL = "/api/v1/envios";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @MockBean EnvioService envioService;
    // Dependencias del filtro JWT (aquí la sesión se simula con authentication(...))
    @MockBean JwtService jwtService;
    @MockBean UsuarioRepository usuarioRepository;

    // ---------- datos de prueba ----------

    private Usuario usuario(Long id, Rol rol) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setRol(rol);
        return u;
    }

    /** Simula la sesión igual que JwtAuthFilter: el principal es la entidad Usuario. */
    private MockHttpServletRequestBuilder como(Usuario u, MockHttpServletRequestBuilder req) {
        var auth = new UsernamePasswordAuthenticationToken(u, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));
        return req.with(authentication(auth));
    }

    private CrearEnvioRequest request(String destinatario, String direccionDestino, double pesoKg) {
        return new CrearEnvioRequest("Juan Pérez", destinatario, "Cra 14 #10-20", direccionDestino,
                1L, 3L, pesoKg, 20, 30, 40);
    }

    private CrearEnvioRequest requestValido() {
        return request("María Gómez", "Cll 5 #3-40", 2.5);
    }

    private EnvioResponse respuesta(Long id) {
        return new EnvioResponse(id, String.format("UQD-%06d", id), EstadoEnvio.REGISTRADO,
                "Juan Pérez", "María Gómez", "Cra 14 #10-20", "Cll 5 #3-40", "Centro", "Sur",
                2.5, 20, 30, 40, 4.37, new BigDecimal("12244.00"), LocalDateTime.now());
    }

    private MockHttpServletRequestBuilder postJson(Object body) throws Exception {
        return post(URL).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
    }

    // ---------- POST /envios ----------

    @Test
    void creaElEnvioYResponde201ConElCodigo() throws Exception {
        Usuario cliente = usuario(10L, Rol.CLIENTE);
        when(envioService.crear(any(), any())).thenReturn(respuesta(123L));

        mvc.perform(como(cliente, postJson(requestValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("UQD-000123"))
                .andExpect(jsonPath("$.estado").value("REGISTRADO"))
                .andExpect(jsonPath("$.valorTotal").value(12244.00));

        // el cliente del envío es el usuario de la sesión, no algo que venga en el body
        verify(envioService).crear(eq(requestValido()), eq(cliente));
    }

    @Test
    void direccionDeDestinoVaciaResponde400ConElCampo() throws Exception {
        mvc.perform(como(usuario(10L, Rol.CLIENTE), postJson(request("María Gómez", "", 2.5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.campos.direccionDestino").exists());

        verifyNoInteractions(envioService);
    }

    @Test
    void pesoCeroResponde400ConElCampo() throws Exception {
        mvc.perform(como(usuario(10L, Rol.CLIENTE), postJson(request("María Gómez", "Cll 5 #3-40", 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.campos.pesoKg").value("Debe ser mayor a 0"));

        verifyNoInteractions(envioService);
    }

    @Test
    void textoConScriptResponde400ConElCampo() throws Exception {
        mvc.perform(como(usuario(10L, Rol.CLIENTE),
                        postJson(request("<script>alert(1)</script>", "Cll 5 #3-40", 2.5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.campos.destinatario").exists());

        verifyNoInteractions(envioService);
    }

    @Test
    void destinoFueraDeCoberturaResponde422() throws Exception {
        when(envioService.crear(any(), any())).thenThrow(
                new AppException("FUERA_DE_COBERTURA", "Destino fuera de cobertura", HttpStatus.UNPROCESSABLE_ENTITY));

        mvc.perform(como(usuario(10L, Rol.CLIENTE), postJson(requestValido())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("FUERA_DE_COBERTURA"))
                .andExpect(jsonPath("$.mensaje").value("Destino fuera de cobertura"))
                .andExpect(jsonPath("$.campos").doesNotExist());
    }

    @Test
    void sinSesionResponde401() throws Exception {
        mvc.perform(postJson(requestValido()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));

        verifyNoInteractions(envioService);
    }

    @Test
    void unRepartidorNoPuedeCrearEnvios() throws Exception {
        mvc.perform(como(usuario(4L, Rol.REPARTIDOR), postJson(requestValido())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("PROHIBIDO"));

        verifyNoInteractions(envioService);
    }

    // ---------- GET /envios ----------

    @Test
    void listaLosEnviosDelClienteAutenticado() throws Exception {
        Usuario cliente = usuario(10L, Rol.CLIENTE);
        when(envioService.listarMios(cliente)).thenReturn(List.of(respuesta(2L), respuesta(1L)));

        mvc.perform(como(cliente, get(URL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].codigo").value("UQD-000002"));
    }

    @Test
    void unAdminNoUsaElListadoDeClientes() throws Exception {
        mvc.perform(como(usuario(1L, Rol.ADMIN), get(URL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("PROHIBIDO"));
    }

    // ---------- GET /envios/{id} ----------

    @Test
    void elClienteVeElDetalleDeSuEnvio() throws Exception {
        Usuario cliente = usuario(10L, Rol.CLIENTE);
        when(envioService.detalle(5L, cliente)).thenReturn(respuesta(5L));

        mvc.perform(como(cliente, get(URL + "/5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("UQD-000005"))
                .andExpect(jsonPath("$.zonaDestino").value("Sur"));
    }

    @Test
    void elEnvioDeOtroClienteResponde403() throws Exception {
        Usuario cliente = usuario(20L, Rol.CLIENTE);
        when(envioService.detalle(5L, cliente)).thenThrow(AppException.prohibido());

        mvc.perform(como(cliente, get(URL + "/5")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("PROHIBIDO"))
                .andExpect(jsonPath("$.mensaje").value("Acceso no autorizado"));
    }

    @Test
    void envioInexistenteResponde404() throws Exception {
        Usuario cliente = usuario(10L, Rol.CLIENTE);
        when(envioService.detalle(99L, cliente)).thenThrow(AppException.noEncontrado("Envío no encontrado"));

        mvc.perform(como(cliente, get(URL + "/99")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
    }
}
