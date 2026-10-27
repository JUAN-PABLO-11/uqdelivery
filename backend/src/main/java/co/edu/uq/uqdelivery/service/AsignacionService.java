package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.AsignarRequest;
import co.edu.uq.uqdelivery.dto.ReasignarRequest;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.*;
import co.edu.uq.uqdelivery.repository.AsignacionRepository;
import co.edu.uq.uqdelivery.repository.EnvioRepository;
import co.edu.uq.uqdelivery.repository.HistorialEstadoRepository;
import co.edu.uq.uqdelivery.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsignacionService {

    private final EnvioRepository envioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AsignacionRepository asignacionRepository;
    private final HistorialEstadoRepository historialRepository;
    private final EstadoService estadoService;

    // Constructor para inyectar dependencias
    public AsignacionService(EnvioRepository envioRepository, UsuarioRepository usuarioRepository,
                             AsignacionRepository asignacionRepository, HistorialEstadoRepository historialRepository,
                             EstadoService estadoService) {
        this.envioRepository = envioRepository;
        this.usuarioRepository = usuarioRepository;
        this.asignacionRepository = asignacionRepository;
        this.historialRepository = historialRepository;
        this.estadoService = estadoService;
    }

    @Transactional
    public void asignarEnvio(Long envioId, AsignarRequest request, Usuario admin) {
        // 1. Buscar el envío
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new AppException("ENVIO_NO_ENCONTRADO", "El envío no existe", HttpStatus.NOT_FOUND));

        // 2. Validar que esté REGISTRADO
        if (envio.getEstado() != EstadoEnvio.REGISTRADO) {
            throw new AppException("TRANSICION_INVALIDA", "El envío no está en estado REGISTRADO", HttpStatus.CONFLICT);
        }

        // 3. Buscar y validar al repartidor
        Usuario repartidor = usuarioRepository.findById(request.repartidorId())
                .orElseThrow(() -> new AppException("REPARTIDOR_NO_EXISTE", "El repartidor no existe", HttpStatus.NOT_FOUND));

        if (repartidor.getRol() != Rol.REPARTIDOR || !repartidor.getActivo() || !repartidor.getDisponible()) {
            throw new AppException("REPARTIDOR_NO_DISPONIBLE", "El repartidor no está disponible o inactivo", HttpStatus.CONFLICT);
        }

        // 4. Crear la asignación activa
        Asignacion asignacion = new Asignacion();
        asignacion.setEnvio(envio);
        asignacion.setRepartidor(repartidor);
        asignacion.setAsignadoPor(admin);
        asignacion.setActiva(true);
        asignacion.setMotivo("Asignación inicial");
        asignacionRepository.save(asignacion);

        // 5. Cambiar el estado del envío usando el EstadoService (Regla de oro del Sprint)
        estadoService.cambiarEstado(envio.getId(), EstadoEnvio.ASIGNADO, admin, "Asignado a " + repartidor.getNombre());
    }

    @Transactional
    public void reasignarEnvio(Long envioId, ReasignarRequest request, Usuario admin) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new AppException("ENVIO_NO_ENCONTRADO", "El envío no existe", HttpStatus.NOT_FOUND));

        if (envio.getEstado() != EstadoEnvio.ASIGNADO) {
            throw new AppException("TRANSICION_INVALIDA", "Solo se pueden reasignar envíos en estado ASIGNADO", HttpStatus.CONFLICT);
        }

        // Buscar la asignación actual
        Asignacion asignacionActual = asignacionRepository.findByEnvioIdAndActivaTrue(envioId)
                .orElseThrow(() -> new AppException("ASIGNACION_NO_ENCONTRADA", "No hay una asignación activa", HttpStatus.NOT_FOUND));

        if (asignacionActual.getRepartidor().getId().equals(request.repartidorId())) {
            throw new AppException("MISMO_REPARTIDOR", "El nuevo repartidor debe ser distinto al actual", HttpStatus.CONFLICT);
        }

        Usuario nuevoRepartidor = usuarioRepository.findById(request.repartidorId())
                .orElseThrow(() -> new AppException("REPARTIDOR_NO_EXISTE", "El repartidor no existe", HttpStatus.NOT_FOUND));

        if (nuevoRepartidor.getRol() != Rol.REPARTIDOR || !nuevoRepartidor.getActivo() || !nuevoRepartidor.getDisponible()) {
            throw new AppException("REPARTIDOR_NO_DISPONIBLE", "El nuevo repartidor no está disponible", HttpStatus.CONFLICT);
        }

        // 1. Inactivar la asignación anterior
        asignacionActual.setActiva(false);
        asignacionRepository.save(asignacionActual);

        // 2. Crear la nueva asignación
        Asignacion nuevaAsignacion = new Asignacion();
        nuevaAsignacion.setEnvio(envio);
        nuevaAsignacion.setRepartidor(nuevoRepartidor);
        nuevaAsignacion.setAsignadoPor(admin);
        nuevaAsignacion.setActiva(true);
        nuevaAsignacion.setMotivo(request.motivo());
        asignacionRepository.save(nuevaAsignacion);

        // 3. Escribir en el historial (La guía dice que la reasignación es el único caso donde se hace a mano)
        HistorialEstado historial = new HistorialEstado();
        historial.setEnvio(envio);
        historial.setEstadoAnterior(EstadoEnvio.ASIGNADO);
        historial.setEstadoNuevo(EstadoEnvio.ASIGNADO);
        historial.setActor(admin);
        historial.setDetalle("Reasignado de " + asignacionActual.getRepartidor().getNombre() +
                " a " + nuevoRepartidor.getNombre() + ". Motivo: " + request.motivo());
        historialRepository.save(historial);
    }
}
