package co.edu.uniquindio.epq.facturacion.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Detalle completo de una factura (CU-05: periodo, lecturas, consumo, valores, vencimiento, estado).
 */
public record FacturaDetalleResponse(
        String numeroFactura,
        String periodo,
        String estado,
        LocalDateTime fechaGeneracion,
        LocalDate fechaVencimiento,
        ContratoResponse contrato,
        String numeroMedidor,
        BigDecimal lecturaAnterior,
        BigDecimal lecturaActual,
        BigDecimal consumo,
        String tarifa,
        BigDecimal valorPorUnidad,
        BigDecimal valorConsumo,
        BigDecimal cargoFijo,
        BigDecimal subtotal,
        BigDecimal total) {
}
