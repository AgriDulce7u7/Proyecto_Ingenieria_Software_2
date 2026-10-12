package co.edu.uniquindio.epq.cartera.dominio;

import java.math.BigDecimal;
import java.util.List;

/**
 * Plan de pagos calculado automáticamente (SWR-30). La suma de las cuotas es igual a la deuda.
 */
public record PlanCuotas(BigDecimal totalDeuda, List<CuotaPlan> cuotas) {

    public PlanCuotas {
        cuotas = List.copyOf(cuotas);
        BigDecimal suma = cuotas.stream().map(CuotaPlan::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (suma.compareTo(totalDeuda) != 0) {
            throw new IllegalArgumentException("La suma de las cuotas (" + suma + ") no coincide con la deuda (" + totalDeuda + ")");
        }
    }

    public int numeroCuotas() {
        return cuotas.size();
    }
}
