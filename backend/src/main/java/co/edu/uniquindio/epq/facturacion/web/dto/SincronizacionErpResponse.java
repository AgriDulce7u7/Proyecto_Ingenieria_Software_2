package co.edu.uniquindio.epq.facturacion.web.dto;

import java.time.LocalDateTime;

public record SincronizacionErpResponse(
        Long id,
        LocalDateTime fechaSincronizacion,
        String estado,
        int intentos,
        String respuestaErp) {
}
