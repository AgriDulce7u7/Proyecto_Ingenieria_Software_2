package co.edu.uniquindio.epq.facturacion.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resultado de una ejecución de facturación mensual (SWR-01, SWR-02, SWR-03, SWR-04).
 */
public record ResultadoGeneracionLoteResponse(
        String periodo,
        String origen,
        String estadoLote,
        LocalDateTime inicio,
        LocalDateTime fin,
        long duracionMs,
        boolean dentroDelTiempoMaximo,
        int contratosActivosEvaluados,
        long contratosNoActivosExcluidos,
        int facturasGeneradas,
        int facturasYaExistentes,
        long totalFacturasDelLote,
        List<IncidenciaFacturacionResponse> incidencias,
        ResultadoSincronizacionResponse sincronizacionErp) {
}
