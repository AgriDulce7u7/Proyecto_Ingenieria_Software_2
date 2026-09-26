package co.edu.uniquindio.epq.pqr.evento;

/**
 * Evento de dominio: la PQR fue asignada o reasignada a un gestor.
 */
public record PqrAsignadaEvento(Long pqrId, Integer gestorId, String motivo, String usuario) {
}
