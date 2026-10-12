package co.edu.uniquindio.epq.dashboard.indicadores;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import co.edu.uniquindio.epq.dashboard.web.dto.IndicadorResponse;
import co.edu.uniquindio.epq.dashboard.web.dto.PuntoTendencia;

/**
 * Utilidades para construir indicadores a partir de su serie mensual.
 */
public final class ConstructorIndicador {

    private ConstructorIndicador() {
    }

    /** El valor actual es el último punto de la serie. */
    public static IndicadorResponse desdeSerie(String codigo, String nombre, String unidad, String sentido,
                                               String descripcion, List<PuntoTendencia> serie) {
        BigDecimal actual = serie.isEmpty() ? BigDecimal.ZERO : serie.get(serie.size() - 1).valor();
        BigDecimal anterior = serie.size() < 2 ? null : serie.get(serie.size() - 2).valor();
        return new IndicadorResponse(codigo, nombre, unidad, actual, variacion(actual, anterior), sentido, descripcion,
                serie);
    }

    /** Indicador con un valor actual distinto del último punto (p. ej. una foto del momento). */
    public static IndicadorResponse conValorActual(String codigo, String nombre, String unidad, String sentido,
                                                   String descripcion, BigDecimal actual, List<PuntoTendencia> serie) {
        BigDecimal anterior = serie.size() < 2 ? null : serie.get(serie.size() - 2).valor();
        return new IndicadorResponse(codigo, nombre, unidad, actual, variacion(actual, anterior), sentido, descripcion,
                serie);
    }

    static BigDecimal variacion(BigDecimal actual, BigDecimal anterior) {
        if (actual == null || anterior == null || anterior.signum() == 0) {
            return null;
        }
        return actual.subtract(anterior).multiply(BigDecimal.valueOf(100)).divide(anterior, 1, RoundingMode.HALF_UP);
    }

    public static BigDecimal porcentaje(long parte, long total) {
        return total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(parte * 100).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }
}
