package co.edu.uq.uqdelivery.controller;

import co.edu.uq.uqdelivery.model.*;
import co.edu.uq.uqdelivery.repository.EnvioRepository;
import co.edu.uq.uqdelivery.repository.UsuarioRepository;
import co.edu.uq.uqdelivery.repository.ZonaRepository;
import co.edu.uq.uqdelivery.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo completo de punta a punta con la seguridad, la BD (H2) y las transacciones reales:
 * HU-06 (asignar y reasignar) -> Caso 00 (iniciar entrega) -> HU-10 (novedad) -> entregado.
 * Además detecta errores de carga perezosa (LazyInitializationException) que las pruebas con mocks no ven.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlujoEntregaIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired EnvioRepository envioRepository;
    @Autowired ZonaRepository zonaRepository;
    @Autowired JwtService jwtService;

    private final String sufijo = UUID.randomUUID().toString().substring(0, 8);

    private Usuario crearUsuario(String nombre, Rol rol) {
        return usuarioRepository.save(new Usuario(nombre + " " + sufijo,
                nombre.toLowerCase() + "-" + sufijo + "@flujo.com", "hash", rol));
    }

    private Envio crearEnvio(Usuario cliente) {
        Zona zona = zonaRepository.save(new Zona("Centro " + sufijo, 4.53, -75.68, true));
        Envio e = new Envio();
        e.setCliente(cliente);
        e.setZonaOrigen(zona);
        e.setZonaDestino(zona);
        e.setRemitente("Remitente");
        e.setDestinatario("Destinatario " + sufijo);
        e.setDireccionOrigen("Cra 14 # 10-20");
        e.setDireccionDestino("Calle 21 # 15-30");
        e.setPesoKg(2);
        e.setValorTotal(new BigDecimal("10000"));
        Envio guardado = envioRepository.save(e);
        guardado.setCodigo("UQD-T" + sufijo);
        return envioRepository.save(guardado);
    }

    private MockHttpServletRequestBuilder como(Usuario u, MockHttpServletRequestBuilder req) {
        return req.header("Authorization", "Bearer " + jwtService.generarToken(u))
                .contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void flujoCompletoDeAsignacionEntregaYNovedad() throws Exception {
        Usuario admin = crearUsuario("Admin", Rol.ADMIN);
        Usuario cliente = crearUsuario("Cliente", Rol.CLIENTE);
        Usuario carlos = crearUsuario("Carlos", Rol.REPARTIDOR);
        Usuario diana = crearUsuario("Diana", Rol.REPARTIDOR);
        Envio envio = crearEnvio(cliente);
        Long id = envio.getId();

        // HU-06: el admin ve el envío sin repartidor y la lista de repartidores
        mvc.perform(como(admin, get("/api/v1/admin/envios").param("estado", "REGISTRADO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].cliente").value(hasItem(cliente.getNombre())));
        mvc.perform(como(admin, get("/api/v1/admin/repartidores")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre", hasItems(carlos.getNombre(), diana.getNombre())));

        // HU-06 criterio 1: asignar -> ASIGNADO y aparece en la lista de Carlos
        mvc.perform(como(admin, post("/api/v1/admin/envios/" + id + "/asignar"))
                        .content("{\"repartidorId\":" + carlos.getId() + "}"))
                .andExpect(status().isOk());
        mvc.perform(como(carlos, get("/api/v1/repartidor/entregas")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id.intValue())))
                .andExpect(jsonPath("$[0].zonaDestino").value("Centro " + sufijo));

        // HU-06 criterio 2: reasignar -> sale de la lista de Carlos y entra a la de Diana
        mvc.perform(como(admin, post("/api/v1/admin/envios/" + id + "/reasignar"))
                        .content("{\"repartidorId\":" + diana.getId() + ",\"motivo\":\"Carlos sin vehículo\"}"))
                .andExpect(status().isOk());
        mvc.perform(como(carlos, get("/api/v1/repartidor/entregas")))
                .andExpect(jsonPath("$[*].id", not(hasItem(id.intValue()))));
        mvc.perform(como(diana, get("/api/v1/repartidor/entregas")))
                .andExpect(jsonPath("$[*].id", hasItem(id.intValue())));
        mvc.perform(como(admin, get("/api/v1/admin/envios")))
                .andExpect(jsonPath("$[?(@.id == " + id + ")].repartidor").value(hasItem(diana.getNombre())));

        // Carlos ya no puede mover el envío
        mvc.perform(como(carlos, patch("/api/v1/repartidor/envios/" + id + "/estado"))
                        .content("{\"estado\":\"EN_RUTA\"}"))
                .andExpect(status().isForbidden());

        // Diana inicia la entrega
        mvc.perform(como(diana, patch("/api/v1/repartidor/envios/" + id + "/estado"))
                        .content("{\"estado\":\"EN_RUTA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_RUTA"));

        // HU-10: novedad con descripción corta -> 400
        mvc.perform(como(diana, post("/api/v1/repartidor/envios/" + id + "/novedades"))
                        .content("{\"tipo\":\"DESTINATARIO_AUSENTE\",\"descripcion\":\"corta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.descripcion").exists());

        // HU-10: novedad válida -> 201 y el envío pasa a CON_NOVEDAD
        mvc.perform(como(diana, post("/api/v1/repartidor/envios/" + id + "/novedades"))
                        .content("{\"tipo\":\"DESTINATARIO_AUSENTE\",\"descripcion\":\"Nadie atiende en la portería\","
                                + "\"latitud\":4.53,\"longitud\":-75.68}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("DESTINATARIO_AUSENTE"));
        mvc.perform(como(diana, get("/api/v1/repartidor/entregas")))
                .andExpect(jsonPath("$[?(@.id == " + id + ")].estado").value(hasItem("CON_NOVEDAD")));

        // Retoma y entrega: sale de la lista de pendientes
        mvc.perform(como(diana, patch("/api/v1/repartidor/envios/" + id + "/estado"))
                        .content("{\"estado\":\"EN_RUTA\",\"detalle\":\"Se retoma la entrega\"}"))
                .andExpect(status().isOk());
        mvc.perform(como(diana, patch("/api/v1/repartidor/envios/" + id + "/estado"))
                        .content("{\"estado\":\"ENTREGADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));
        mvc.perform(como(diana, get("/api/v1/repartidor/entregas")))
                .andExpect(jsonPath("$[*].id", not(hasItem(id.intValue()))));
    }

    @Test
    void unClienteNoPuedeUsarLosEndpointsDeAdministrador() throws Exception {
        Usuario cliente = crearUsuario("Intruso", Rol.CLIENTE);

        mvc.perform(como(cliente, get("/api/v1/admin/envios")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("PROHIBIDO"));
        mvc.perform(como(cliente, patch("/api/v1/admin/usuarios/1/estado")).content("{\"activo\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminFiltraUsuariosPorRolYReactivaUnaCuenta() throws Exception {
        Usuario admin = crearUsuario("Jefe", Rol.ADMIN);
        Usuario rep = crearUsuario("Pedro", Rol.REPARTIDOR);

        mvc.perform(como(admin, get("/api/v1/admin/usuarios").param("rol", "REPARTIDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].rol", everyItem(is("REPARTIDOR"))))
                .andExpect(jsonPath("$[*].nombre", hasItem(rep.getNombre())));

        mvc.perform(como(admin, patch("/api/v1/admin/usuarios/" + rep.getId() + "/estado")).content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(como(admin, patch("/api/v1/admin/usuarios/" + rep.getId() + "/estado")).content("{\"activo\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }
}
