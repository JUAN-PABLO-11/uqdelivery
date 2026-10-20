package co.edu.uq.uqdelivery.config;

import co.edu.uq.uqdelivery.model.*;
import co.edu.uq.uqdelivery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Datos de prueba para que todos puedan trabajar desde el día 1.
 * Solo corre si la tabla de usuarios está vacía y nunca en el perfil "test".
 * Contraseña de todos los usuarios: Uqdelivery123*
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    public static final String PASSWORD_PRUEBA = "Uqdelivery123*";

    private final UsuarioRepository usuarioRepository;
    private final ZonaRepository zonaRepository;
    private final TarifaRepository tarifaRepository;
    private final EnvioRepository envioRepository;
    private final AsignacionRepository asignacionRepository;
    private final HistorialEstadoRepository historialRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) return;

        String hash = passwordEncoder.encode(PASSWORD_PRUEBA);
        Usuario admin = usuarioRepository.save(new Usuario("Admin UQ", "admin@uqdelivery.com", hash, Rol.ADMIN));
        Usuario cliente1 = usuarioRepository.save(new Usuario("Laura Gómez", "cliente1@uqdelivery.com", hash, Rol.CLIENTE));
        Usuario cliente2 = usuarioRepository.save(new Usuario("Andrés Ruiz", "cliente2@uqdelivery.com", hash, Rol.CLIENTE));
        Usuario rep1 = usuarioRepository.save(new Usuario("Carlos Repartidor", "repartidor1@uqdelivery.com", hash, Rol.REPARTIDOR));
        Usuario rep2 = usuarioRepository.save(new Usuario("Diana Repartidora", "repartidor2@uqdelivery.com", hash, Rol.REPARTIDOR));

        // Zonas de Armenia (lat/lng aproximadas). Circasia inactiva -> prueba de "fuera de cobertura" (HU-05)
        Zona centro = zonaRepository.save(new Zona("Centro", 4.5339, -75.6811, true));
        Zona norte = zonaRepository.save(new Zona("Norte", 4.5550, -75.6620, true));
        Zona sur = zonaRepository.save(new Zona("Sur", 4.5150, -75.6900, true));
        Zona uq = zonaRepository.save(new Zona("Universidad del Quindío", 4.5546, -75.6593, true));
        zonaRepository.save(new Zona("Calarcá", 4.5296, -75.6430, true));
        zonaRepository.save(new Zona("Circasia", 4.6186, -75.6358, false));

        tarifaRepository.save(new Tarifa(new BigDecimal("5000"), new BigDecimal("1200"), new BigDecimal("800")));

        // Envíos en distintos estados
        crearEnvio(cliente1, centro, norte, EstadoEnvio.REGISTRADO, null, admin);
        crearEnvio(cliente1, sur, uq, EstadoEnvio.REGISTRADO, null, admin);
        crearEnvio(cliente2, norte, sur, EstadoEnvio.REGISTRADO, null, admin);
        crearEnvio(cliente1, uq, centro, EstadoEnvio.ASIGNADO, rep1, admin);
        crearEnvio(cliente2, centro, sur, EstadoEnvio.ASIGNADO, rep2, admin);
        crearEnvio(cliente1, norte, centro, EstadoEnvio.EN_RUTA, rep1, admin);
        crearEnvio(cliente2, sur, norte, EstadoEnvio.EN_RUTA, rep1, admin);
        crearEnvio(cliente1, centro, uq, EstadoEnvio.ENTREGADO, rep2, admin);

        log.info("DataSeeder: datos de prueba cargados (contraseña: {})", PASSWORD_PRUEBA);
    }

    private void crearEnvio(Usuario cliente, Zona origen, Zona destino, EstadoEnvio estadoFinal,
                            Usuario repartidor, Usuario admin) {
        Envio e = new Envio();
        e.setCliente(cliente);
        e.setZonaOrigen(origen);
        e.setZonaDestino(destino);
        e.setRemitente(cliente.getNombre());
        e.setDestinatario("Destinatario de prueba");
        e.setDireccionOrigen("Cra 14 # 10-20, " + origen.getNombre());
        e.setDireccionDestino("Calle 21 # 15-30, " + destino.getNombre());
        e.setPesoKg(2.5);
        e.setAltoCm(20);
        e.setAnchoCm(30);
        e.setLargoCm(40);
        e.setDistanciaKm(3.0);
        e.setValorTotal(new BigDecimal("10600"));
        e.setEstado(estadoFinal);
        Envio guardado = envioRepository.save(e);
        guardado.setCodigo(String.format("UQD-%06d", guardado.getId()));

        historialRepository.save(new HistorialEstado(guardado, null, EstadoEnvio.REGISTRADO, cliente, "Envío creado"));
        if (repartidor != null) {
            asignacionRepository.save(new Asignacion(guardado, repartidor, admin, "Asignación inicial"));
            historialRepository.save(new HistorialEstado(guardado, EstadoEnvio.REGISTRADO, EstadoEnvio.ASIGNADO, admin, "Asignado a " + repartidor.getNombre()));
        }
        if (estadoFinal == EstadoEnvio.EN_RUTA || estadoFinal == EstadoEnvio.ENTREGADO) {
            historialRepository.save(new HistorialEstado(guardado, EstadoEnvio.ASIGNADO, EstadoEnvio.EN_RUTA, repartidor, "Inicio de entrega"));
        }
        if (estadoFinal == EstadoEnvio.ENTREGADO) {
            historialRepository.save(new HistorialEstado(guardado, EstadoEnvio.EN_RUTA, EstadoEnvio.ENTREGADO, repartidor, "Entregado"));
        }
    }
}
