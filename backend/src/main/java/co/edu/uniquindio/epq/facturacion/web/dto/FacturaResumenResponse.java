package co.edu.uniquindio.epq.facturacion.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @param estado      estado de sincronización con el ERP (entrega 1)
 * @param estadoCobro estado de cobro DE-01: PENDIENTE, PAGADA, VENCIDA o ANULADA
 */
public record FacturaResumenResponse(
        String numeroFactura,
        String periodo,
        String numeroContrato,
        String cliente,
        String servicio,
        BigDecimal consumo,
        BigDecimal total,
        LocalDate fechaVencimiento,
        String estado,
        String estadoCobro) {

    /** Constructor de la entrega 1 (sin estado de cobro), conservado por compatibilidad. */
    public FacturaResumenResponse(String numeroFactura, String periodo, String numeroContrato, String cliente,
                                  String servicio, BigDecimal consumo, BigDecimal total, LocalDate fechaVencimiento,
                                  String estado) {
        this(numeroFactura, periodo, numeroContrato, cliente, servicio, consumo, total, fechaVencimiento, estado, null);
    }
}
