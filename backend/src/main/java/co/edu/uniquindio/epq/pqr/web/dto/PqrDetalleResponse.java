package co.edu.uniquindio.epq.pqr.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detalle completo de una PQR para el back-office.
 *
 * @param accionesDisponibles estados a los que puede pasar la PQR; útil para que el frontend
 *                            habilite o deshabilite botones.
 */
public record PqrDetalleResponse(
        Long id,
        String radicado,
        String tipoSolicitud,
        String canal,
        String estado,
        String asunto,
        String descripcion,
        CiudadanoResponse ciudadano,
        GestorResponse gestor,
        LocalDateTime fechaRecepcion,
        LocalDateTime fechaLimiteRespuesta,
        LocalDate fechaEstimadaRespuesta,
        LocalDateTime fechaResolucion,
        String respuesta,
        boolean alertaVencimientoEnviada,
        List<String> accionesDisponibles) {
}
