package co.edu.uniquindio.epq.pqr.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SyR-02f / RN-04: cumplimiento del plazo normativo para el reporte de indicadores a la SSPD.
 */
public record CumplimientoPqrResponse(
        LocalDate desde,
        LocalDate hasta,
        Totales totales,
        List<Agrupado> porTipo,
        List<Agrupado> porMunicipio,
        List<Incumplimiento> incumplimientos) {

    public record Totales(long radicadas, long respondidas, long respondidasEnPlazo, long respondidasFueraDePlazo,
                          long vencidasSinRespuesta, BigDecimal porcentajeCumplimiento) {
    }

    public record Agrupado(String grupo, long radicadas, long respondidasEnPlazo, long incumplidas,
                           BigDecimal porcentajeCumplimiento) {
    }

    public record Incumplimiento(String radicado, String tipoSolicitud, String municipio, String gestor,
                                 LocalDateTime fechaRecepcion, LocalDateTime fechaLimite, LocalDateTime fechaRespuesta,
                                 String estado) {
    }
}
