package co.edu.uniquindio.epq.facturacion.servicio;

import co.edu.uniquindio.epq.facturacion.web.dto.ResultadoSincronizacionResponse;
import co.edu.uniquindio.epq.facturacion.servicio.sincronizacion.ResultadoSincronizacionFactura;

/**
 * Sincronización de facturas con el ERP financiero (SWR-04, IS-01).
 */
public interface SincronizacionErpService {

    /** Sincroniza las facturas pendientes o con error del lote del periodo indicado. */
    ResultadoSincronizacionResponse sincronizarLote(String periodo);

    /** Reintenta la sincronización de una factura puntual. */
    ResultadoSincronizacionFactura sincronizarFactura(String numeroFactura);
}
