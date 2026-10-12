package co.edu.uniquindio.epq.cartera.dominio;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Reglas puras de mora (sin dependencias de infraestructura, fáciles de probar).
 */
public final class CalculadoraMora {

    private CalculadoraMora() {
    }

    /**
     * RN-03: cantidad de períodos consecutivos en mora que terminan en el período vencido más reciente.
     * Ej.: {2026-06, 2026-07, 2026-08} → 3; {2026-03, 2026-07, 2026-08} → 2.
     */
    public static int periodosConsecutivos(Collection<YearMonth> periodosVencidos) {
        if (periodosVencidos == null || periodosVencidos.isEmpty()) {
            return 0;
        }
        TreeSet<YearMonth> ordenados = new TreeSet<>(periodosVencidos);
        YearMonth actual = ordenados.last();
        int consecutivos = 0;
        while (ordenados.contains(actual)) {
            consecutivos++;
            actual = actual.minusMonths(1);
        }
        return consecutivos;
    }

    public static long antiguedadDias(LocalDate vencimientoMasAntiguo, LocalDate hoy) {
        return vencimientoMasAntiguo == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(vencimientoMasAntiguo, hoy));
    }

    /** Rango de antigüedad de la deuda para el reporte de cartera. */
    public static String rangoMora(long dias) {
        if (dias <= 30) {
            return "0-30 días";
        }
        if (dias <= 60) {
            return "31-60 días";
        }
        if (dias <= 90) {
            return "61-90 días";
        }
        return "Más de 90 días";
    }

    /** SWR-32: candidato a suspensión si tiene N o más períodos consecutivos en mora y ningún acuerdo vigente. */
    public static boolean esCandidatoSuspension(int periodosConsecutivos, boolean acuerdoVigente, int umbral) {
        return !acuerdoVigente && periodosConsecutivos >= umbral;
    }
}
