package co.edu.uniquindio.epq.pagos.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(
        String codigoPago,
        String numeroComprobante,
        String numeroFactura,
        String numeroContrato,
        String usuario,
        BigDecimal valor,
        String canal,
        LocalDateTime fechaPago,
        String codigoTransaccion,
        String registradoPor) {
}
