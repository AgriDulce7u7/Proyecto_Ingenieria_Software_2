package co.edu.uniquindio.epq.pqr.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Confirmación mostrada al ciudadano tras radicar la PQR (CU-06 paso 4 / DS-02 paso 10).
 */
public record PqrRegistradaResponse(
        String radicado,
        String tipoSolicitud,
        String estado,
        LocalDateTime fechaRecepcion,
        LocalDateTime fechaLimiteRespuesta,
        LocalDate fechaEstimadaRespuesta,
        String gestorAsignado,
        String mensaje) {
}
