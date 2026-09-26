package co.edu.uniquindio.epq.facturacion.servicio.sincronizacion;

/**
 * Resultado de sincronizar una factura con el ERP.
 */
public record ResultadoSincronizacionFactura(String numeroFactura, boolean exitosa, int intentos, String mensaje) {
}
