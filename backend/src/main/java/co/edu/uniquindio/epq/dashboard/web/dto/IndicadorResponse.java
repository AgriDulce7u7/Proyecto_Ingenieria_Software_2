package co.edu.uniquindio.epq.dashboard.web.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Indicador del dashboard con su valor actual y la tendencia de los últimos 12 meses (SWR-35).
 *
 * @param variacionPorcentual variación del último punto respecto al anterior (nulo si no aplica)
 * @param sentidoDeseado      "SUBIR" o "BAJAR": ayuda al frontend a colorear la variación
 */
public record IndicadorResponse(
        String codigo,
        String nombre,
        String unidad,
        BigDecimal valorActual,
        BigDecimal variacionPorcentual,
        String sentidoDeseado,
        String descripcion,
        List<PuntoTendencia> tendencia) {
}
