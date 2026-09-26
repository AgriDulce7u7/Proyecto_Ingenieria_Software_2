package co.edu.uniquindio.epq.facturacion.dominio;

import java.math.BigDecimal;

/**
 * Resultado inmutable del cálculo de una factura (valor objeto).
 */
public record DetalleLiquidacion(
        BigDecimal consumo,
        BigDecimal valorConsumo,
        BigDecimal cargoFijo,
        BigDecimal subtotal,
        BigDecimal total) {
}
