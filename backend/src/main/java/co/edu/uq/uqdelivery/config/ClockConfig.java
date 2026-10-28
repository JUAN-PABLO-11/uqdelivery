package co.edu.uq.uqdelivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Reloj del sistema como bean. Los services que dependen de la hora (p. ej. el bloqueo de
 * cuenta de HU-01) lo reciben inyectado, así en las pruebas se puede usar un reloj fijo.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
