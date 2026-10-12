package co.edu.uniquindio.epq.pagos.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SWR-22: comprobante electrónico de pago que se muestra inmediatamente después de la confirmación.
 */
public record ComprobantePagoResponse(
        String numeroComprobante,
        String codigoPago,
        LocalDateTime fechaPago,
        String numeroFactura,
        String periodo,
        String numeroContrato,
        String servicio,
        String usuario,
        String documentoUsuario,
        BigDecimal valor,
        String canal,
        String codigoTransaccion,
        String referencia,
        String registradoPor,
        String estadoFactura) {
}
