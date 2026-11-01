package co.edu.uq.uqdelivery.service;

import co.edu.uq.uqdelivery.dto.request.CrearEnvioRequest;
import co.edu.uq.uqdelivery.dto.response.CotizacionResponse;
import co.edu.uq.uqdelivery.dto.response.EnvioResponse;
import co.edu.uq.uqdelivery.exception.AppException;
import co.edu.uq.uqdelivery.model.Envio;
import co.edu.uq.uqdelivery.model.EstadoEnvio;
import co.edu.uq.uqdelivery.model.Usuario;
import co.edu.uq.uqdelivery.model.Zona;
import co.edu.uq.uqdelivery.repository.EnvioRepository;
import co.edu.uq.uqdelivery.repository.ZonaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * HU-04: creación y consulta de envíos del cliente.
 * Las respuestas se arman dentro de la transacción porque las zonas se cargan de forma perezosa.
 */
@Service
@RequiredArgsConstructor
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final ZonaRepository zonaRepository;
    private final CotizacionService cotizacionService;   // HU-05
    private final EstadoService estadoService;           // Caso 00

    /**
     * Crea el envío en estado REGISTRADO con su código de seguimiento.
     * Si la zona está fuera de cobertura, cotizar() lanza 422 y no se guarda nada.
     */
    @Transactional
    public EnvioResponse crear(CrearEnvioRequest request, Usuario cliente) {
        // 1. El valor y la distancia los calcula el backend (nunca se confía en el frontend)
        CotizacionResponse cotizacion = cotizacionService.cotizar(request.aCotizacion());

        // 2. Armar el envío. El cliente sale del token.
        Envio envio = new Envio();
        envio.setCliente(cliente);
        // Única excepción a la regla de estados: la creación no pasa por cambiarEstado() (guía 3.5)
        envio.setEstado(EstadoEnvio.REGISTRADO);
        envio.setRemitente(request.remitente().trim());
        envio.setDestinatario(request.destinatario().trim());
        envio.setDireccionOrigen(request.direccionOrigen().trim());
        envio.setDireccionDestino(request.direccionDestino().trim());
        envio.setZonaOrigen(buscarZona(request.zonaOrigenId()));
        envio.setZonaDestino(buscarZona(request.zonaDestinoId()));
        envio.setPesoKg(request.pesoKg());
        envio.setAltoCm(request.altoCm());
        envio.setAnchoCm(request.anchoCm());
        envio.setLargoCm(request.largoCm());
        envio.setDistanciaKm(cotizacion.distanciaKm());
        envio.setValorTotal(cotizacion.total());

        // 3. Guardar y generar el código con el id que asignó la BD (misma transacción)
        Envio guardado = envioRepository.save(envio);
        guardado.setCodigo(String.format("UQD-%06d", guardado.getId()));

        // 4. Historial: estado anterior null -> REGISTRADO, actor = cliente
        estadoService.registrarCreacion(guardado, cliente);

        return EnvioResponse.de(guardado);
    }

    /** Envíos del cliente autenticado, del más reciente al más antiguo. */
    @Transactional(readOnly = true)
    public List<EnvioResponse> listarMios(Usuario cliente) {
        return envioRepository.findByClienteIdOrderByCreadoEnDesc(cliente.getId())
                .stream()
                .map(EnvioResponse::de)
                .toList();
    }

    /** Detalle de un envío: 404 si no existe, 403 si no es del cliente. */
    @Transactional(readOnly = true)
    public EnvioResponse detalle(Long envioId, Usuario cliente) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> AppException.noEncontrado("Envío no encontrado"));

        if (!envio.getCliente().getId().equals(cliente.getId())) {
            throw AppException.prohibido();
        }
        return EnvioResponse.de(envio);
    }

    /** cotizar() ya validó que la zona exista y esté activa; aquí solo se carga para el envío. */
    private Zona buscarZona(Long zonaId) {
        return zonaRepository.findById(zonaId)
                .orElseThrow(() -> AppException.noEncontrado("Zona no encontrada"));
    }
}
