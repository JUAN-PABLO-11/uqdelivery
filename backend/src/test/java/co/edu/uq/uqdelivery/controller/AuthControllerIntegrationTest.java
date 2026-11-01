package co.edu.uq.uqdelivery.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-01 de punta a punta: controller + validación + service + BD H2 + seguridad.
 * Cada prueba usa un correo distinto para no depender de las otras.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    private static final String PASSWORD = "Segura123*";

    @Autowired MockMvc mvc;

    private ResultActions registrar(String nombre, String correo, String password) throws Exception {
        String body = """
                {"nombre":"%s","correo":"%s","password":"%s"}""".formatted(nombre, correo, password);
        return mvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions login(String correo, String password) throws Exception {
        String body = """
                {"correo":"%s","password":"%s"}""".formatted(correo, password);
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void registroExitosoDevuelve201SinExponerLaPassword() throws Exception {
        registrar("Ana Cliente", "ana@correo.com", PASSWORD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("ana@correo.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registroYLuegoLoginDevuelveToken() throws Exception {
        registrar("Beto", "beto@correo.com", PASSWORD).andExpect(status().isCreated());

        login("beto@correo.com", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void correoRepetidoDevuelve409() throws Exception {
        registrar("Caro", "caro@correo.com", PASSWORD).andExpect(status().isCreated());

        registrar("Caro 2", "CARO@correo.com", PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_DUPLICADO"));
    }

    @Test
    void passwordDebilDevuelve400ConElCampo() throws Exception {
        registrar("Dani", "dani@correo.com", "debil")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.campos.password").exists());
    }

    @Test
    void nombreConHtmlSeRechaza() throws Exception {
        registrar("<script>alert(1)</script>", "eva@correo.com", PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nombre").exists());
    }

    @Test
    void cincoIntentosFallidosBloqueanLaCuentaYElSextoRecibe423() throws Exception {
        registrar("Fede", "fede@correo.com", PASSWORD).andExpect(status().isCreated());

        for (int i = 0; i < 5; i++) {
            login("fede@correo.com", "Incorrecta1*")
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
        }

        // Sexto intento, aunque la contraseña sea la correcta
        login("fede@correo.com", PASSWORD)
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.codigo").value("CUENTA_BLOQUEADA"))
                .andExpect(jsonPath("$.mensaje").value(startsWith("Cuenta bloqueada temporalmente")));
    }
}
