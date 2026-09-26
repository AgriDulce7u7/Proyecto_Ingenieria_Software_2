package co.edu.uniquindio.epq.comun.calendario;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Abstracción del calendario de días hábiles (DIP).
 *
 * <p>Los servicios de PQR (plazos normativos, RN-04) y de facturación (primer día hábil del
 * mes, SWR-01) dependen de esta interfaz y no de una implementación concreta.</p>
 */
public interface CalendarioLaboral {

    boolean esDiaHabil(LocalDate fecha);

    /**
     * Suma días hábiles a una fecha. El conteo inicia el día siguiente a {@code fechaInicio},
     * como se cuentan los términos legales en Colombia.
     */
    LocalDate sumarDiasHabiles(LocalDate fechaInicio, int dias);

    LocalDate primerDiaHabil(YearMonth mes);

    default boolean esPrimerDiaHabilDelMes(LocalDate fecha) {
        return fecha.equals(primerDiaHabil(YearMonth.from(fecha)));
    }
}
