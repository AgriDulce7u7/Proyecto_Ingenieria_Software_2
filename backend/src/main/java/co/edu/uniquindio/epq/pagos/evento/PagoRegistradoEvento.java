package co.edu.uniquindio.epq.pagos.evento;

/** Evento de dominio: se confirmó y registró un pago. */
public record PagoRegistradoEvento(Long pagoId, String codigoPago) {
}
