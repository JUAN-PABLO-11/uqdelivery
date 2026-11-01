package co.edu.uq.uqdelivery.dto;

import co.edu.uq.uqdelivery.dto.request.CotizacionRequest;
import co.edu.uq.uqdelivery.dto.request.CrearEnvioRequest;
import co.edu.uq.uqdelivery.dto.response.EnvioResponse;
import co.edu.uq.uqdelivery.model.Envio;
import co.edu.uq.uqdelivery.model.EstadoEnvio;
import co.edu.uq.uqdelivery.model.Zona;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** HU-04, criterio 2: la API rechaza dirección vacía, peso <= 0 y texto con HTML/script. */
class CrearEnvioRequestTest {

    private static jakarta.validation.ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void iniciar() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrar() {
        factory.close();
    }

    private CrearEnvioRequest request(String destinatario, String direccionDestino, double pesoKg) {
        return new CrearEnvioRequest("Juan Pérez", destinatario, "Cra 14 #10-20", direccionDestino,
                1L, 3L, pesoKg, 20, 30, 40);
    }

    private Set<String> camposInvalidos(CrearEnvioRequest r) {
        return validator.validate(r).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void aceptaUnEnvioConDatosValidos() {
        assertTrue(camposInvalidos(request("María Gómez", "Cll 5 #3-40", 2.5)).isEmpty());
    }

    @Test
    void rechazaDireccionDeDestinoVacia() {
        assertEquals(Set.of("direccionDestino"), camposInvalidos(request("María Gómez", "  ", 2.5)));
    }

    @Test
    void rechazaPesoCeroONegativo() {
        assertEquals(Set.of("pesoKg"), camposInvalidos(request("María Gómez", "Cll 5 #3-40", 0)));
        assertEquals(Set.of("pesoKg"), camposInvalidos(request("María Gómez", "Cll 5 #3-40", -1)));
    }

    @Test
    void rechazaTextoConScript() {
        assertEquals(Set.of("destinatario"),
                camposInvalidos(request("<script>alert(1)</script>", "Cll 5 #3-40", 2.5)));
    }

    @Test
    void rechazaZonasYMedidasFaltantes() {
        CrearEnvioRequest r = new CrearEnvioRequest("Juan", "María", "Cra 1", "Cll 2",
                null, null, 1, 0, 0, 0);
        assertEquals(Set.of("zonaOrigenId", "zonaDestinoId", "altoCm", "anchoCm", "largoCm"), camposInvalidos(r));
    }

    @Test
    void convierteLosDatosDelPaqueteParaLaCotizacion() {
        CotizacionRequest c = request("María Gómez", "Cll 5 #3-40", 2.5).aCotizacion();
        assertEquals(new CotizacionRequest(1L, 3L, 2.5, 20, 30, 40), c);
    }

    @Test
    void laRespuestaMuestraLasZonasPorNombre() {
        Envio envio = new Envio();
        envio.setId(7L);
        envio.setCodigo("UQD-000007");
        envio.setZonaOrigen(new Zona("Centro", 4.53, -75.68, true));
        envio.setZonaDestino(new Zona("Norte", 4.55, -75.66, true));
        envio.setValorTotal(new BigDecimal("16240.00"));

        EnvioResponse r = EnvioResponse.de(envio);

        assertEquals("UQD-000007", r.codigo());
        assertEquals(EstadoEnvio.REGISTRADO, r.estado());
        assertEquals("Centro", r.zonaOrigen());
        assertEquals("Norte", r.zonaDestino());
    }
}
