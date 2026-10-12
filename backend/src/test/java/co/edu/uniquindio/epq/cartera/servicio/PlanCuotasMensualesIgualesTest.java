package co.edu.uniquindio.epq.cartera.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.cartera.CarteraProperties;
import co.edu.uniquindio.epq.cartera.dominio.PlanCuotas;
import co.edu.uniquindio.epq.comun.calendario.CalendarioLaboralColombia;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;

class PlanCuotasMensualesIgualesTest {

    private final PlanCuotasMensualesIguales politica = new PlanCuotasMensualesIguales(
            new CarteraProperties(2, new BigDecimal("50000"), 2, 12, 30, "0 30 2 * * *"),
            new CalendarioLaboralColombia());

    private final LocalDate hoy = LocalDate.of(2026, 10, 11);

    @Test
    void laUltimaCuotaAbsorbeElRedondeoYLaSumaEsExacta() {
        PlanCuotas plan = politica.calcular(new BigDecimal("100000"), 3, LocalDate.of(2026, 11, 10), hoy);

        assertEquals(3, plan.numeroCuotas());
        assertEquals(new BigDecimal("33333.33"), plan.cuotas().get(0).valor());
        assertEquals(new BigDecimal("33333.34"), plan.cuotas().get(2).valor());
        assertEquals(0, new BigDecimal("100000").compareTo(plan.cuotas().stream()
                .map(c -> c.valor()).reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    @Test
    void losVencimientosQueCaenEnDiaNoHabilPasanAlSiguienteDiaHabil() {
        // 7 nov 2026 es sábado → lunes 9; 7 dic es lunes → 7 dic (8 dic es festivo, no afecta)
        PlanCuotas plan = politica.calcular(new BigDecimal("120000"), 2, LocalDate.of(2026, 11, 7), hoy);
        assertEquals(LocalDate.of(2026, 11, 9), plan.cuotas().get(0).fechaVencimiento());
        assertEquals(LocalDate.of(2026, 12, 7), plan.cuotas().get(1).fechaVencimiento());
    }

    @Test
    void sinNumeroDeCuotasSeCalculaConLaCuotaMinimaDentroDelRango() {
        assertEquals(3, politica.numeroAutomatico(new BigDecimal("120000")));  // ceil(2.4)
        assertEquals(2, politica.numeroAutomatico(new BigDecimal("30000")));   // mínimo 2
        assertEquals(12, politica.numeroAutomatico(new BigDecimal("9000000"))); // máximo 12
    }

    @Test
    void rechazaCuotasFueraDeRangoYPrimeraCuotaNoFutura() {
        assertThrows(ReglaNegocioException.class,
                () -> politica.calcular(new BigDecimal("100000"), 13, null, hoy));
        assertThrows(ReglaNegocioException.class,
                () -> politica.calcular(new BigDecimal("100000"), 2, hoy, hoy));
        assertThrows(ReglaNegocioException.class,
                () -> politica.calcular(BigDecimal.ZERO, 2, null, hoy));
    }
}
