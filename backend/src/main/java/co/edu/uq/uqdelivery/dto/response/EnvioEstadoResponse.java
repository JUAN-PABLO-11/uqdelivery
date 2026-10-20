package co.edu.uq.uqdelivery.dto.response;

import co.edu.uq.uqdelivery.model.Envio;
import co.edu.uq.uqdelivery.model.EstadoEnvio;

public record EnvioEstadoResponse(Long id, String codigo, EstadoEnvio estado) {
    public static EnvioEstadoResponse de(Envio e) {
        return new EnvioEstadoResponse(e.getId(), e.getCodigo(), e.getEstado());
    }
}
