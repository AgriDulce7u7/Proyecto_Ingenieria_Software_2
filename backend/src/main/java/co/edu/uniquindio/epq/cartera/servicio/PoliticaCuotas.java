package co.edu.uniquindio.epq.cartera.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;

import co.edu.uniquindio.epq.cartera.dominio.PlanCuotas;

/**
 * Patrón <b>Strategy</b>: cálculo automático del plan de cuotas de un acuerdo (SWR-30).
 * Otras políticas (cuota inicial, plazos según estrato...) se agregan sin tocar el servicio (OCP).
 */
public interface PoliticaCuotas {

    /**
     * @param numeroCuotasSolicitado opcional; si es nulo la política decide el número de cuotas
     * @param fechaPrimeraCuota      opcional; si es nula la política la calcula
     */
    PlanCuotas calcular(BigDecimal totalDeuda, Integer numeroCuotasSolicitado, LocalDate fechaPrimeraCuota,
                        LocalDate hoy);
}
