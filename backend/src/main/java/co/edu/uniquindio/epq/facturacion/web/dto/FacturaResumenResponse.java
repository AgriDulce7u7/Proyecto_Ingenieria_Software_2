package co.edu.uniquindio.epq.facturacion.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FacturaResumenResponse(
        String numeroFactura,
        String periodo,
        String numeroContrato,
        String cliente,
        String servicio,
        BigDecimal consumo,
        BigDecimal total,
        LocalDate fechaVencimiento,
        String estado) {
}
