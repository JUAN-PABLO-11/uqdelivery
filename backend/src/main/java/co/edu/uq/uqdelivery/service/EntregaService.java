package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.response.EntregaResponse;
import co.edu.uq.uqdelivery.model.Asignacion;
import co.edu.uq.uqdelivery.model.Envio;
import co.edu.uq.uqdelivery.model.EstadoEnvio;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.repository.AsignacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntregaService {

    private static final Set<EstadoEnvio> PENDIENTES =
            Set.of(EstadoEnvio.ASIGNADO, EstadoEnvio.EN_RUTA, EstadoEnvio.CON_NOVEDAD);

    private final AsignacionRepository asignacionRepository;

    /**
     * Entregas pendientes del repartidor (ASIGNADO, EN_RUTA o CON_NOVEDAD), de la más antigua a la más reciente.
     * readOnly = true: el envío y su zona se cargan de forma perezosa y open-in-view está apagado.
     */
    @Transactional(readOnly = true)
    public List<EntregaResponse> obtenerMisEntregas(Usuario repartidor) {
        List<Asignacion> asignaciones = asignacionRepository.findByRepartidorIdAndActivaTrue(repartidor.getId());

        return asignaciones.stream()
                .map(Asignacion::getEnvio)
                .filter(envio -> PENDIENTES.contains(envio.getEstado()))
                .sorted(Comparator.comparing(Envio::getCreadoEn, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(envio -> new EntregaResponse(
                        envio.getId(),
                        envio.getCodigo(),
                        envio.getEstado(),
                        envio.getDireccionDestino(),
                        envio.getZonaDestino() != null ? envio.getZonaDestino().getNombre() : null,
                        envio.getDestinatario(),
                        envio.getCreadoEn()
                ))
                .collect(Collectors.toList());
    }
}