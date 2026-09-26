package co.edu.uniquindio.epq.facturacion.erp;

/**
 * Puerto de salida hacia el ERP financiero (IS-01). Patrón <b>Adapter</b>.
 *
 * <p>La lógica de facturación depende de esta abstracción (DIP). Hoy existen dos adaptadores:
 * uno simulado para el prototipo y uno REST para un ERP real; cambiar de uno a otro es solo
 * configuración ({@code app.erp.modo}).</p>
 */
public interface ErpFinancieroGateway {

    /**
     * Envía una factura al ERP. No debe lanzar excepciones por errores de negocio del ERP;
     * las fallas técnicas (timeout, conexión) pueden propagarse como {@link RuntimeException}.
     */
    RespuestaErp enviarFactura(FacturaErp factura);
}
