package co.edu.uniquindio.epq.facturacion.servicio.calculo;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.facturacion.dominio.Contrato;
import co.edu.uniquindio.epq.facturacion.dominio.DetalleLiquidacion;
import co.edu.uniquindio.epq.facturacion.dominio.LecturaMedidor;
import co.edu.uniquindio.epq.facturacion.dominio.Tarifa;

/**
 * Estrategia general (SWR-03): {@code valor = consumo × valor por unidad + cargo fijo}.
 * Aplica a cualquier servicio medido; los valores se redondean a 2 decimales (HALF_UP).
 */
@Component
public class CalculoPorConsumoMedido implements EstrategiaCalculoFactura {

    private static final int ESCALA = 2;

    @Override
    public boolean aplicaA(Contrato contrato) {
        return true;
    }

    @Override
    public int prioridad() {
        return Integer.MAX_VALUE; // estrategia por defecto
    }

    @Override
    public DetalleLiquidacion calcular(LecturaMedidor lectura, Tarifa tarifa) {
        return calcular(lectura.getConsumo(), tarifa.getValorPorUnidad(), tarifa.getCargoFijo());
    }

    /** Cálculo puro, independiente de JPA (facilita las pruebas unitarias). */
    public DetalleLiquidacion calcular(BigDecimal consumo, BigDecimal valorPorUnidad, BigDecimal cargoFijo) {
        if (consumo == null || consumo.signum() < 0) {
            throw new ReglaNegocioException("El consumo registrado no puede ser negativo: " + consumo);
        }
        BigDecimal consumoNormalizado = consumo.setScale(ESCALA, RoundingMode.HALF_UP);
        BigDecimal valorConsumo = consumoNormalizado.multiply(valorPorUnidad).setScale(ESCALA, RoundingMode.HALF_UP);
        BigDecimal cargo = cargoFijo == null ? BigDecimal.ZERO.setScale(ESCALA)
                : cargoFijo.setScale(ESCALA, RoundingMode.HALF_UP);
        BigDecimal subtotal = valorConsumo.add(cargo);
        // En este prototipo no se manejan subsidios ni contribuciones: total = subtotal.
        return new DetalleLiquidacion(consumoNormalizado, valorConsumo, cargo, subtotal, subtotal);
    }
}
