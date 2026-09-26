package co.edu.uniquindio.epq.facturacion.erp;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representación de una factura en el formato de intercambio con el ERP (DTO de integración).
 * Aísla el modelo de dominio del contrato externo.
 */
public record FacturaErp(
        String numeroFactura,
        String periodo,
        String numeroContrato,
        String documentoCliente,
        String servicio,
        BigDecimal consumo,
        BigDecimal subtotal,
        BigDecimal total,
        LocalDate fechaVencimiento) {
}
