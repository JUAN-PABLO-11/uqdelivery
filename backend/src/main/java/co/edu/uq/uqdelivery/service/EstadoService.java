package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.*;
import co.edu.uq.uqdelivery.repository.AsignacionRepository;
import co.edu.uq.uqdelivery.repository.EnvioRepository;
import co.edu.uq.uqdelivery.repository.HistorialEstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * ÚNICO lugar donde cambia el estado de un envío. REGLA DEL EQUIPO: nadie hace envio.setEstado(...).
 *
 *  REGISTRADO  -> ASIGNADO     (ADMIN, HU-06)
 *  ASIGNADO    -> EN_RUTA      (REPARTIDOR asignado, "Iniciar entrega")
 *  EN_RUTA     -> CON_NOVEDAD  (REPARTIDOR asignado, HU-10)
 *  EN_RUTA     -> ENTREGADO    (REPARTIDOR asignado)
 *  CON_NOVEDAD -> EN_RUTA      (REPARTIDOR asignado, "Retomar")
 *  CON_NOVEDAD -> ENTREGADO    (REPARTIDOR asignado)
 *  Reasignación (ASIGNADO -> ASIGNADO): usar registrarReasignacion().
 *  CANCELADO: se agregará con HU-13.
 */
@Service
@RequiredArgsConstructor
public class EstadoService {

    private static final Map<EstadoEnvio, Map<EstadoEnvio, Rol>> TRANSICIONES = Map.of(
            EstadoEnvio.REGISTRADO, Map.of(EstadoEnvio.ASIGNADO, Rol.ADMIN),
            EstadoEnvio.ASIGNADO, Map.of(EstadoEnvio.EN_RUTA, Rol.REPARTIDOR),
            EstadoEnvio.EN_RUTA, Map.of(
                    EstadoEnvio.CON_NOVEDAD, Rol.REPARTIDOR,
                    EstadoEnvio.ENTREGADO, Rol.REPARTIDOR),
            EstadoEnvio.CON_NOVEDAD, Map.of(
                    EstadoEnvio.EN_RUTA, Rol.REPARTIDOR,
                    EstadoEnvio.ENTREGADO, Rol.REPARTIDOR),
            EstadoEnvio.ENTREGADO, Map.of(),
            EstadoEnvio.CANCELADO, Map.of());

    private final EnvioRepository envioRepository;
    private final HistorialEstadoRepository historialRepository;
    private final AsignacionRepository asignacionRepository;

    @Transactional
    public Envio cambiarEstado(Long envioId, EstadoEnvio nuevo, Usuario actor, String detalle) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> AppException.noEncontrado("Envío no encontrado"));

        EstadoEnvio anterior = envio.getEstado();
        Rol rolRequerido = TRANSICIONES.getOrDefault(anterior, Map.of()).get(nuevo);
        if (rolRequerido == null) {
            throw new AppException("TRANSICION_INVALIDA",
                    "No se puede pasar de " + anterior + " a " + nuevo, HttpStatus.CONFLICT);
        }
        if (actor.getRol() != rolRequerido) {
            throw AppException.prohibido();
        }
        // Además del rol: el repartidor solo puede mover envíos que tiene asignados
        if (actor.getRol() == Rol.REPARTIDOR
                && !asignacionRepository.existsByEnvioIdAndRepartidorIdAndActivaTrue(envio.getId(), actor.getId())) {
            throw AppException.prohibido();
        }

        envio.setEstado(nuevo);
        envioRepository.save(envio);
        historialRepository.save(new HistorialEstado(envio, anterior, nuevo, actor, detalle));
        return envio;
    }

    /** HU-06: la reasignación no cambia el estado (sigue ASIGNADO) pero queda en el historial. */
    @Transactional
    public void registrarReasignacion(Envio envio, Usuario actor, String detalle) {
        if (actor.getRol() != Rol.ADMIN) {
            throw AppException.prohibido();
        }
        if (envio.getEstado() != EstadoEnvio.ASIGNADO) {
            throw new AppException("TRANSICION_INVALIDA",
                    "Solo se puede reasignar un envío en estado ASIGNADO", HttpStatus.CONFLICT);
        }
        historialRepository.save(new HistorialEstado(envio, EstadoEnvio.ASIGNADO, EstadoEnvio.ASIGNADO, actor, detalle));
    }

    /** HU-04: registra la creación del envío en el historial (estado anterior = null). */
    @Transactional
    public void registrarCreacion(Envio envio, Usuario cliente) {
        historialRepository.save(new HistorialEstado(envio, null, EstadoEnvio.REGISTRADO, cliente, "Envío creado"));
    }
}
