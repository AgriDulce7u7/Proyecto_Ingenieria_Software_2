package co.edu.uniquindio.epq.pqr.evento;

import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;

/**
 * Evento de dominio: la PQR cambió de estado (trámite, respuesta, cierre o vencimiento).
 */
public record PqrEstadoCambiadoEvento(Long pqrId, EstadoPqrTipo estadoAnterior, EstadoPqrTipo estadoNuevo,
                                      String comentario, String usuario) {
}
