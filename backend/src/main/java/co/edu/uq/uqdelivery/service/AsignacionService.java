package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.AsignarRequest;
import co.edu.uq.uqdelivery.dto.request.ReasignarRequest;
import co.edu.uq.uqdelivery.dto.response.AdminEnvioResponse;
import co.edu.uq.uqdelivery.dto.response.RepartidorResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.*;
import co.edu.uq.uqdelivery.repository.AsignacionRepository;
import co.edu.uq.uqdelivery.repository.EnvioRepository;
import co.edu.uq.uqdelivery.repository.HistorialEstadoRepository;
import co.edu.uq.uqdelivery.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsignacionService {

    private final EnvioRepository envioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionRepository asignacionRepository;
    private final HistorialEstadoRepository historialRepository;
    private final EstadoService estadoService;

    public AsignacionService(EnvioRepository envioRepository, UsuarioRepository usuarioRepository,
                             AsignacionRepository asignacionRepository, HistorialEstadoRepository historialRepository,
                             EstadoService estadoService) {
        this.envioRepository = envioRepository;
        this.usuarioRepository = usuarioRepository;
        this.asignacionRepository = asignacionRepository;
        this.historialRepository = historialRepository;
        this.estadoService = estadoService;
    }

    // ==========================================
    // MÉTODOS DEL ADMINISTRADOR (Asignar/Reasignar)
    // ==========================================

    @Transactional
    public void asignarEnvio(Long envioId, AsignarRequest request, Usuario admin) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new AppException("ENVIO_NO_ENCONTRADO", "El envío no existe", HttpStatus.NOT_FOUND));

        if (envio.getEstado() != EstadoEnvio.REGISTRADO) {
            throw new AppException("TRANSICION_INVALIDA", "El envío no está en estado REGISTRADO", HttpStatus.CONFLICT);
        }

        Usuario repartidor = usuarioRepository.findById(request.repartidorId())
                .orElseThrow(() -> new AppException("REPARTIDOR_NO_EXISTE", "El repartidor no existe", HttpStatus.NOT_FOUND));

        if (repartidor.getRol() != Rol.REPARTIDOR || !repartidor.isActivo() || !repartidor.isDisponible()) {
            throw new AppException("REPARTIDOR_NO_DISPONIBLE", "El repartidor no está disponible o inactivo", HttpStatus.CONFLICT);
        }

        Asignacion asignacion = new Asignacion();
        asignacion.setEnvio(envio);
        asignacion.setRepartidor(repartidor);
        asignacion.setAsignadoPor(admin);
        asignacion.setActiva(true);
        asignacion.setMotivo("Asignación inicial");
        asignacionRepository.save(asignacion);

        estadoService.cambiarEstado(envio.getId(), EstadoEnvio.ASIGNADO, admin, "Asignado a " + repartidor.getNombre());
    }

    @Transactional
    public void reasignarEnvio(Long envioId, ReasignarRequest request, Usuario admin) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new AppException("ENVIO_NO_ENCONTRADO", "El envío no existe", HttpStatus.NOT_FOUND));

        if (envio.getEstado() != EstadoEnvio.ASIGNADO) {
            throw new AppException("TRANSICION_INVALIDA", "Solo se pueden reasignar envíos en estado ASIGNADO", HttpStatus.CONFLICT);
        }

        Asignacion asignacionActual = asignacionRepository.findByEnvioIdAndActivaTrue(envioId)
                .orElseThrow(() -> new AppException("ASIGNACION_NO_ENCONTRADA", "No hay una asignación activa", HttpStatus.NOT_FOUND));

        if (asignacionActual.getRepartidor().getId().equals(request.repartidorId())) {
            throw new AppException("MISMO_REPARTIDOR", "El nuevo repartidor debe ser distinto al actual", HttpStatus.CONFLICT);
        }

        Usuario nuevoRepartidor = usuarioRepository.findById(request.repartidorId())
                .orElseThrow(() -> new AppException("REPARTIDOR_NO_EXISTE", "El repartidor no existe", HttpStatus.NOT_FOUND));

        if (nuevoRepartidor.getRol() != Rol.REPARTIDOR || !nuevoRepartidor.isActivo() || !nuevoRepartidor.isDisponible()) {
            throw new AppException("REPARTIDOR_NO_DISPONIBLE", "El nuevo repartidor no está disponible", HttpStatus.CONFLICT);
        }

        asignacionActual.setActiva(false);
        asignacionRepository.save(asignacionActual);

        Asignacion nuevaAsignacion = new Asignacion();
        nuevaAsignacion.setEnvio(envio);
        nuevaAsignacion.setRepartidor(nuevoRepartidor);
        nuevaAsignacion.setAsignadoPor(admin);
        nuevaAsignacion.setActiva(true);
        nuevaAsignacion.setMotivo(request.motivo());
        asignacionRepository.save(nuevaAsignacion);

        HistorialEstado historial = new HistorialEstado();
        historial.setEnvio(envio);
        historial.setEstadoAnterior(EstadoEnvio.ASIGNADO);
        historial.setEstadoNuevo(EstadoEnvio.ASIGNADO);
        historial.setActor(admin);
        historial.setDetalle("Reasignado de " + asignacionActual.getRepartidor().getNombre() +
                " a " + nuevoRepartidor.getNombre() + ". Motivo: " + request.motivo());
        historialRepository.save(historial);
    }

    // ==========================================
    // MÉTODOS GET DEL ADMINISTRADOR (Listados)
    // ==========================================

    /**
     * readOnly = true: open-in-view está apagado, así que cliente, zona y repartidor (carga perezosa)
     * solo se pueden leer dentro de una transacción.
     */
    @Transactional(readOnly = true)
    public List<AdminEnvioResponse> listarEnviosAdmin(EstadoEnvio estadoFiltro) {
        List<Envio> envios = (estadoFiltro != null)
                ? envioRepository.findByEstadoOrderByCreadoEnDesc(estadoFiltro)
                : envioRepository.findAll(Sort.by(Sort.Direction.DESC, "creadoEn"));

        return envios.stream().map(envio -> {
            // Repartidor de la asignación activa (null si aún no tiene)
            Usuario repartidor = asignacionRepository.findByEnvioIdAndActivaTrue(envio.getId())
                    .map(Asignacion::getRepartidor)
                    .orElse(null);

            return new AdminEnvioResponse(
                    envio.getId(),
                    envio.getCodigo(),
                    envio.getEstado(),
                    envio.getDestinatario(),
                    envio.getDireccionDestino(),
                    envio.getZonaDestino() != null ? envio.getZonaDestino().getNombre() : null,
                    envio.getCliente().getNombre(),
                    repartidor != null ? repartidor.getId() : null,
                    repartidor != null ? repartidor.getNombre() : null,
                    envio.getCreadoEn()
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RepartidorResponse> listarRepartidoresDisponibles() {
        return usuarioRepository.findByRolAndActivoTrueAndDisponibleTrue(Rol.REPARTIDOR)
                .stream()
                .map(u -> new RepartidorResponse(u.getId(), u.getNombre()))
                .collect(Collectors.toList());
    }
}