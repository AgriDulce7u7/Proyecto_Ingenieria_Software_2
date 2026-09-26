package co.edu.uniquindio.epq.facturacion.servicio.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.facturacion.dominio.DetalleLiquidacion;

class CalculoPorConsumoMedidoTest {

    private final CalculoPorConsumoMedido estrategia = new CalculoPorConsumoMedido();

    @Test
    void valorEsConsumoPorTarifaMasCargoFijo() {
        DetalleLiquidacion detalle = estrategia.calcular(
                new BigDecimal("18.00"), new BigDecimal("3215.50"), new BigDecimal("12650.00"));
        assertEquals(new BigDecimal("57879.00"), detalle.valorConsumo());
        assertEquals(new BigDecimal("12650.00"), detalle.cargoFijo());
        assertEquals(new BigDecimal("70529.00"), detalle.subtotal());
        assertEquals(new BigDecimal("70529.00"), detalle.total());
    }

    @Test
    void redondeaMedioHaciaArribaADosDecimales() {
        DetalleLiquidacion detalle = estrategia.calcular(
                new BigDecimal("12.35"), new BigDecimal("2870.25"), BigDecimal.ZERO);
        // 12.35 × 2870.25 = 35447.5875 → 35447.59
        assertEquals(new BigDecimal("35447.59"), detalle.valorConsumo());
    }

    @Test
    void consumoCeroSoloCobraCargoFijo() {
        DetalleLiquidacion detalle = estrategia.calcular(
                BigDecimal.ZERO, new BigDecimal("2415.80"), new BigDecimal("4520.00"));
        assertEquals(new BigDecimal("4520.00"), detalle.total());
    }

    @Test
    void rechazaConsumoNegativo() {
        assertThrows(ReglaNegocioException.class, () -> estrategia.calcular(
                new BigDecimal("-1"), new BigDecimal("100"), BigDecimal.ZERO));
    }
}
