package co.edu.uq.uqdelivery.dto.response;

import co.edu.uq.uqdelivery.model.TipoNovedad;
import java.time.LocalDateTime;

public record NovedadResponse(
        Long id,
        Long envioId,
        TipoNovedad tipo,
        String descripcion,
        Double latitud,
        Double longitud,
        LocalDateTime creadoEn
) {}