package co.edu.uniquindio.epq.pqr.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detalle completo de una PQR para el back-office.
 *
 * @param accionesDisponibles estados a los que puede pasar la PQR; útil para que el frontend
 *                            habilite o deshabilite botones.
 * @param municipio           municipio donde se presta el servicio (entrega 2, asignación por territorio).
 * @param numeroContrato      contrato asociado a la PQR, si el ciudadano lo informó.
 * @param respondidaEnPlazo   {@code true}/{@code false} cuando ya fue respondida o venció; {@code null} si sigue abierta.
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
        List<String> accionesDisponibles,
        String municipio,
        String numeroContrato,
        Boolean respondidaEnPlazo) {

    /** Compatibilidad con la primera entrega. */
    public PqrDetalleResponse(Long id, String radicado, String tipoSolicitud, String canal, String estado,
                              String asunto, String descripcion, CiudadanoResponse ciudadano, GestorResponse gestor,
                              LocalDateTime fechaRecepcion, LocalDateTime fechaLimiteRespuesta,
                              LocalDate fechaEstimadaRespuesta, LocalDateTime fechaResolucion, String respuesta,
                              boolean alertaVencimientoEnviada, List<String> accionesDisponibles) {
        this(id, radicado, tipoSolicitud, canal, estado, asunto, descripcion, ciudadano, gestor, fechaRecepcion,
                fechaLimiteRespuesta, fechaEstimadaRespuesta, fechaResolucion, respuesta, alertaVencimientoEnviada,
                accionesDisponibles, null, null, null);
    }
}
