package co.edu.uniquindio.epq.facturacion.servicio;

import co.edu.uniquindio.epq.facturacion.web.dto.FacturaDetalleResponse;

/**
 * DE-01 anularFactura(motivo): anulación por error de liquidación u orden administrativa (RF-15 la audita).
 */
public interface AnulacionFacturaService {

    FacturaDetalleResponse anular(String numeroFactura, String motivo);
}
