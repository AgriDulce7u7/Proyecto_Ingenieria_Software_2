package co.edu.uniquindio.epq.pqr.web.dto;

import java.time.LocalDateTime;

/**
 * StR-02c: PQR del equipo con su situación frente al plazo normativo.
 *
 * @param nivel VENCIDA, CRITICA (≤ 48 h), EN_RIESGO (≤ 5 días) o EN_PLAZO
 */
public record AlertaPqrResponse(
        String radicado,
        String tipoSolicitud,
        String estado,
        String asunto,
        String gestor,
        Integer gestorId,
        String municipio,
        LocalDateTime fechaLimiteRespuesta,
        long horasRestantes,
        String nivel,
        boolean alertaEnviada) {
}
