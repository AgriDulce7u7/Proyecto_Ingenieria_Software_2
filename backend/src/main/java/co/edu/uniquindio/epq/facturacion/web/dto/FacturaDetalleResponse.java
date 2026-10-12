package co.edu.uniquindio.epq.facturacion.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Detalle completo de una factura (CU-05 / SWR-25: periodo, lecturas, consumo, valores, vencimiento y estado).
 * {@code estado} es la sincronización con el ERP; {@code estadoCobro} el ciclo de cobro (DE-01).
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
        BigDecimal total,
        String estadoCobro,
        LocalDateTime fechaPago,
        String motivoAnulacion,
        boolean enAcuerdoDePago) {

    /** Constructor de la entrega 1, conservado por compatibilidad. */
    public FacturaDetalleResponse(String numeroFactura, String periodo, String estado, LocalDateTime fechaGeneracion,
                                  LocalDate fechaVencimiento, ContratoResponse contrato, String numeroMedidor,
                                  BigDecimal lecturaAnterior, BigDecimal lecturaActual, BigDecimal consumo,
                                  String tarifa, BigDecimal valorPorUnidad, BigDecimal valorConsumo,
                                  BigDecimal cargoFijo, BigDecimal subtotal, BigDecimal total) {
        this(numeroFactura, periodo, estado, fechaGeneracion, fechaVencimiento, contrato, numeroMedidor, lecturaAnterior,
                lecturaActual, consumo, tarifa, valorPorUnidad, valorConsumo, cargoFijo, subtotal, total, null, null,
                null, false);
    }
}
