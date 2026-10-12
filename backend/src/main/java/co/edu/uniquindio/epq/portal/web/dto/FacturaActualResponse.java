package co.edu.uniquindio.epq.portal.web.dto;

import java.time.LocalDate;

import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;
import co.edu.uniquindio.epq.facturacion.web.dto.FacturaResumenResponse;

/**
 * Factura del período actual (SWR-25). Si aún no se ha generado, {@code disponible} es falso y se informa
 * la fecha estimada de disponibilidad (SWR-28, CU-05 4a) junto con la última factura emitida.
 */
public record FacturaActualResponse(
        String numeroContrato,
        String periodoActual,
        boolean disponible,
        FacturaDetalleResponse factura,
        LocalDate fechaEstimadaDisponibilidad,
        String mensaje,
        FacturaResumenResponse ultimaFacturaEmitida) {
}
