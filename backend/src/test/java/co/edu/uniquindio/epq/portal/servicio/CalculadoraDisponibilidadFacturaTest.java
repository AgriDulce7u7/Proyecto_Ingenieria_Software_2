package co.edu.uniquindio.epq.portal.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.comun.calendario.CalendarioLaboralColombia;

/** SWR-28 / RN-02: la factura del mes está disponible 2 días hábiles después del primer día hábil. */
class CalculadoraDisponibilidadFacturaTest {

    private final CalculadoraDisponibilidadFactura calculadora =
            new CalculadoraDisponibilidadFactura(new CalendarioLaboralColombia());

    @Test
    void elPeriodoActualEsElMesDeConsumoAnterior() {
        assertEquals(YearMonth.of(2026, 9), calculadora.periodoActual(LocalDate.of(2026, 10, 11)));
        assertEquals(YearMonth.of(2025, 12), calculadora.periodoActual(LocalDate.of(2026, 1, 5)));
    }

    @Test
    void estimaDosDiasHabilesDespuesDelInicioDelCiclo() {
        // Agosto 2026: el 1 es sábado → ciclo inicia lunes 3 → disponible miércoles 5
        assertEquals(LocalDate.of(2026, 8, 5), calculadora.fechaEstimada(LocalDate.of(2026, 8, 1)));
        // Enero 2026: el 1 es festivo → inicia viernes 2 → +2 hábiles = martes 6 (lunes 5 es hábil; Reyes se traslada al 12)
        assertEquals(LocalDate.of(2026, 1, 6), calculadora.fechaEstimada(LocalDate.of(2026, 1, 2)));
    }

    @Test
    void siElCicloYaDebioTerminarEstimaElSiguienteDiaHabil() {
        // 9 oct 2026 (viernes), el ciclo de octubre terminó el 5 → siguiente hábil: martes 13 (12 es festivo)
        assertEquals(LocalDate.of(2026, 10, 13), calculadora.fechaEstimada(LocalDate.of(2026, 10, 9)));
    }
}
