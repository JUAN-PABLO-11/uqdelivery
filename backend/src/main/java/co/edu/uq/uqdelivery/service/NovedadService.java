package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.CrearNovedadRequest;
import co.edu.uq.uqdelivery.dto.response.NovedadResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.Envio;
import co.edu.uq.uqdelivery.model.Novedad;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.model.EstadoEnvio;
import co.edu.uq.uqdelivery.repository.AsignacionRepository;
import co.edu.uq.uqdelivery.repository.EnvioRepository;
import co.edu.uq.uqdelivery.repository.NovedadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NovedadService {

    private final NovedadRepository novedadRepository;
    private final EnvioRepository envioRepository;
    private final AsignacionRepository asignacionRepository;
    private final EstadoService estadoService;

    @Transactional
    public NovedadResponse registrarNovedad(Long envioId, CrearNovedadRequest request, Usuario repartidor) {

        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> AppException.noEncontrado("El envío especificado no existe"));

        boolean esSuEntrega = asignacionRepository.existsByEnvioIdAndRepartidorIdAndActivaTrue(envioId, repartidor.getId());
        if (!esSuEntrega) {
            throw AppException.prohibido();
        }

        if (envio.getEstado() != EstadoEnvio.EN_RUTA) {
            throw new AppException("TRANSICION_INVALIDA",
                    "Solo se pueden reportar novedades para envíos que se encuentren en ruta", HttpStatus.CONFLICT);
        }

        Novedad novedad = new Novedad();
        novedad.setEnvio(envio);
        novedad.setRepartidor(repartidor);
        novedad.setTipo(request.tipo());
        novedad.setDescripcion(request.descripcion());
        novedad.setLatitud(request.latitud());
        novedad.setLongitud(request.longitud());
        novedad.setCreadoEn(LocalDateTime.now());

        Novedad guardada = novedadRepository.save(novedad);

        String detalle = request.tipo() + ": " + request.descripcion();
        estadoService.cambiarEstado(envioId, EstadoEnvio.CON_NOVEDAD, repartidor, detalle);

        return new NovedadResponse(
                guardada.getId(),
                envio.getId(),
                guardada.getTipo(),
                guardada.getDescripcion(),
                guardada.getLatitud(),
                guardada.getLongitud(),
                guardada.getCreadoEn()
        );
    }
}