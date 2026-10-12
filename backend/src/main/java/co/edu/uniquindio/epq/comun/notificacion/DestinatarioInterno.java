package co.edu.uniquindio.epq.comun.notificacion;

/**
 * Funcionario que debe recibir una notificación interna.
 */
public record DestinatarioInterno(Integer funcionarioId, String nombre, String correo) {
}
