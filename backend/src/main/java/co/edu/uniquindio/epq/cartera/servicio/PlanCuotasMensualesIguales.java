package co.edu.uniquindio.epq.cartera.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.cartera.CarteraProperties;
import co.edu.uniquindio.epq.cartera.dominio.CuotaPlan;
import co.edu.uniquindio.epq.cartera.dominio.PlanCuotas;
import co.edu.uniquindio.epq.comun.calendario.CalendarioLaboral;
import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;

/**
 * Cuotas mensuales iguales (la última absorbe el redondeo). Si no se indica el número de cuotas, se
 * calcula con la cuota mínima configurada, dentro del rango permitido. Los vencimientos que caen en un
 * día no hábil se trasladan al siguiente día hábil.
 */
@Component
public class PlanCuotasMensualesIguales implements PoliticaCuotas {

    private final CarteraProperties propiedades;
    private final CalendarioLaboral calendario;

    public PlanCuotasMensualesIguales(CarteraProperties propiedades, CalendarioLaboral calendario) {
        this.propiedades = propiedades;
        this.calendario = calendario;
    }

    @Override
    public PlanCuotas calcular(BigDecimal totalDeuda, Integer numeroCuotasSolicitado, LocalDate fechaPrimeraCuota,
                               LocalDate hoy) {
        if (totalDeuda == null || totalDeuda.signum() <= 0) {
            throw new ReglaNegocioException("La deuda del acuerdo debe ser mayor que cero");
        }
        int cuotas = numeroCuotasSolicitado != null ? numeroCuotasSolicitado : numeroAutomatico(totalDeuda);
        if (cuotas < propiedades.minCuotas() || cuotas > propiedades.maxCuotas()) {
            throw new ReglaNegocioException("El número de cuotas debe estar entre %d y %d"
                    .formatted(propiedades.minCuotas(), propiedades.maxCuotas()));
        }
        LocalDate primera = fechaPrimeraCuota != null ? fechaPrimeraCuota : hoy.plusDays(propiedades.diasPrimeraCuota());
        if (!primera.isAfter(hoy)) {
            throw new ReglaNegocioException("La primera cuota debe vencer después de hoy");
        }

        BigDecimal valorCuota = totalDeuda.divide(BigDecimal.valueOf(cuotas), 2, RoundingMode.HALF_UP);
        List<CuotaPlan> plan = new ArrayList<>();
        for (int i = 1; i <= cuotas; i++) {
            BigDecimal valor = i < cuotas ? valorCuota
                    : totalDeuda.subtract(valorCuota.multiply(BigDecimal.valueOf(cuotas - 1L)));
            plan.add(new CuotaPlan(i, valor, siguienteHabil(primera.plusMonths(i - 1L))));
        }
        return new PlanCuotas(totalDeuda, plan);
    }

    /** ceil(deuda / cuota mínima) acotado al rango permitido. */
    int numeroAutomatico(BigDecimal totalDeuda) {
        int calculado = totalDeuda.divide(propiedades.cuotaMinima(), 0, RoundingMode.CEILING).intValue();
        return Math.min(propiedades.maxCuotas(), Math.max(propiedades.minCuotas(), calculado));
    }

    private LocalDate siguienteHabil(LocalDate fecha) {
        return calendario.esDiaHabil(fecha) ? fecha : calendario.sumarDiasHabiles(fecha, 1);
    }
}
