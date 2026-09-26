package co.edu.uniquindio.epq.pqr.evento;

/**
 * Evento de dominio: se radicó una nueva PQR.
 */
public record PqrRegistradaEvento(Long pqrId, String radicado, String usuario) {
}
