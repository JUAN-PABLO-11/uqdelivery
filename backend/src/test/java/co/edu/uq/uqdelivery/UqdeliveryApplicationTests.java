package co.edu.uq.uqdelivery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Si esta prueba falla, la aplicación no arranca (configuración, entidades o beans rotos). */
@SpringBootTest
@ActiveProfiles("test")
class UqdeliveryApplicationTests {

    @Test
    void contextLoads() {
    }
}
