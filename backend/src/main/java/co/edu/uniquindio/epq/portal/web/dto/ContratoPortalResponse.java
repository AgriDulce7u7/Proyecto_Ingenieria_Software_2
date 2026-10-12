package co.edu.uniquindio.epq.portal.web.dto;

import java.math.BigDecimal;

/**
 * Contrato del usuario en el panel del portal (CU-05 paso 2).
 *
 * @param saldoPendiente suma de las facturas pendientes o vencidas
 */
public record ContratoPortalResponse(
        String numeroContrato,
        String servicio,
        String direccionServicio,
        String municipio,
        String estado,
        String ultimaFactura,
        String ultimoPeriodoFacturado,
        BigDecimal saldoPendiente,
        long facturasVencidas) {
}
