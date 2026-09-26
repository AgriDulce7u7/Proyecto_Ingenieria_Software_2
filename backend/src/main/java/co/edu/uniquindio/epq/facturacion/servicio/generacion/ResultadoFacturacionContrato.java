package co.edu.uniquindio.epq.facturacion.servicio.generacion;

/**
 * Resultado de facturar un contrato.
 */
public record ResultadoFacturacionContrato(
        Integer contratoId,
        String numeroContrato,
        TipoResultadoFacturacion tipo,
        String numeroFactura,
        String detalle) {

    public static ResultadoFacturacionContrato de(Integer contratoId, String numeroContrato,
                                                  TipoResultadoFacturacion tipo) {
        return new ResultadoFacturacionContrato(contratoId, numeroContrato, tipo, null, tipo.getDescripcion());
    }
}
