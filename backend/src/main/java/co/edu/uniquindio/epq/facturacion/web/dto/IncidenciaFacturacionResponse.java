package co.edu.uniquindio.epq.facturacion.web.dto;

/**
 * Contrato activo que no pudo facturarse en el lote, con su causa.
 */
public record IncidenciaFacturacionResponse(String numeroContrato, String tipo, String detalle) {
}
