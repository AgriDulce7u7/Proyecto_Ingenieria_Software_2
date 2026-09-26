package co.edu.uniquindio.epq.facturacion.web.dto;

import java.util.List;

/**
 * Resumen de la sincronización de un conjunto de facturas con el ERP (SWR-04).
 */
public record ResultadoSincronizacionResponse(
        String periodo,
        int facturasProcesadas,
        int exitosas,
        int fallidas,
        List<String> facturasConError) {
}
