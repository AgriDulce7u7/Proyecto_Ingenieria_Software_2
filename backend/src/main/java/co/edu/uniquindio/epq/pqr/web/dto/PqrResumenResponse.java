package co.edu.uniquindio.epq.pqr.web.dto;

import java.time.LocalDateTime;

/**
 * Fila de la bandeja de PQR del gestor.
 */
public record PqrResumenResponse(
        String radicado,
        String tipoSolicitud,
        String estado,
        String asunto,
        String ciudadano,
        String gestor,
        LocalDateTime fechaRecepcion,
        LocalDateTime fechaLimiteRespuesta,
        boolean alertaVencimientoEnviada) {
}
