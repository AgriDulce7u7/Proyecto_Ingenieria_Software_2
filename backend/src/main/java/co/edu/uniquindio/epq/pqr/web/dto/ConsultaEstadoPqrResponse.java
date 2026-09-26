package co.edu.uniquindio.epq.pqr.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Consulta pública por radicado, sin inicio de sesión (RF-10 / SWR-08).
 * Por privacidad (Ley 1581 de 2012) NO expone datos personales ni el contenido de la PQR.
 */
public record ConsultaEstadoPqrResponse(
        String radicado,
        String tipoSolicitud,
        String estado,
        LocalDateTime fechaRecepcion,
        LocalDate fechaEstimadaRespuesta,
        LocalDateTime fechaResolucion) {
}
