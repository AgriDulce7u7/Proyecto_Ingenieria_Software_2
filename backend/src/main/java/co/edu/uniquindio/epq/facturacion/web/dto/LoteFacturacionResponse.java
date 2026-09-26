package co.edu.uniquindio.epq.facturacion.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoteFacturacionResponse(
        Integer id,
        String periodo,
        String estado,
        LocalDateTime fechaInicioProceso,
        LocalDateTime fechaFinProceso,
        Long duracionSegundos,
        int totalFacturasGeneradas,
        long facturasSincronizadas,
        long facturasConErrorSincronizacion,
        long facturasPendientesSincronizacion,
        BigDecimal valorTotalFacturado) {
}
