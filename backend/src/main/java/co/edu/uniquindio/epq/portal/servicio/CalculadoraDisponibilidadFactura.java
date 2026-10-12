package co.edu.uniquindio.epq.portal.servicio;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.comun.calendario.CalendarioLaboral;

/**
 * SWR-28: fecha estimada en que estará disponible la factura del período actual.
 * RN-02: el ciclo de facturación inicia el primer día hábil del mes y debe completarse en 48 horas.
 */
@Component
public class CalculadoraDisponibilidadFactura {

    /** RN-02: 48 horas = 2 días hábiles después del inicio del ciclo. */
    static final int DIAS_HABILES_CICLO = 2;

    private final CalendarioLaboral calendario;

    public CalculadoraDisponibilidadFactura(CalendarioLaboral calendario) {
        this.calendario = calendario;
    }

    /** El período que corresponde facturar en el mes de {@code hoy} es el mes de consumo anterior. */
    public YearMonth periodoActual(LocalDate hoy) {
        return YearMonth.from(hoy).minusMonths(1);
    }

    public LocalDate fechaEstimada(LocalDate hoy) {
        LocalDate inicioCiclo = calendario.primerDiaHabil(YearMonth.from(hoy));
        LocalDate finCiclo = calendario.sumarDiasHabiles(inicioCiclo, DIAS_HABILES_CICLO);
        // Si el ciclo ya debió terminar (retraso), se estima el siguiente día hábil
        return hoy.isAfter(finCiclo) ? calendario.sumarDiasHabiles(hoy, 1) : finCiclo;
    }
}
