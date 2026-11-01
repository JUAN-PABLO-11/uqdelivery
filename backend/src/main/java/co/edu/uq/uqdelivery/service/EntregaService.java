package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.response.EntregaResponse;
import co.edu.uq.uqdelivery.model.Asignacion;
import co.edu.uq.uqdelivery.model.EstadoEnvio;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.repository.AsignacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntregaService {

    private final AsignacionRepository asignacionRepository;

    public List<EntregaResponse> obtenerMisEntregas(Usuario repartidor) {
        List<Asignacion> asignaciones = asignacionRepository.findByRepartidorIdAndActivaTrue(repartidor.getId());

        return asignaciones.stream()
                .map(Asignacion::getEnvio)
                .filter(envio -> envio.getEstado() == EstadoEnvio.ASIGNADO ||
                        envio.getEstado() == EstadoEnvio.EN_RUTA ||
                        envio.getEstado() == EstadoEnvio.CON_NOVEDAD)
                .map(envio -> new EntregaResponse(
                        envio.getId(),
                        envio.getCodigo(),
                        envio.getEstado(),
                        envio.getDireccionDestino(),
                        envio.getDestinatario(),
                        envio.getCreadoEn()
                ))
                .collect(Collectors.toList());
    }
}