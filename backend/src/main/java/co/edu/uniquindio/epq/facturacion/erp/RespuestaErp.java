package co.edu.uniquindio.epq.facturacion.erp;

/**
 * Respuesta del ERP ante el envío de una factura.
 *
 * @param exitosa    si el ERP aceptó la factura
 * @param referencia identificador asignado por el ERP (solo si fue exitosa)
 * @param mensaje    detalle devuelto por el ERP
 */
public record RespuestaErp(boolean exitosa, String referencia, String mensaje) {

    public static RespuestaErp exito(String referencia, String mensaje) {
        return new RespuestaErp(true, referencia, mensaje);
    }

    public static RespuestaErp fallo(String mensaje) {
        return new RespuestaErp(false, null, mensaje);
    }
}
