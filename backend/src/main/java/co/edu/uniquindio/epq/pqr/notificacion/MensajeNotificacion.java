package co.edu.uniquindio.epq.pqr.notificacion;

/**
 * Mensaje a enviar por un canal de notificación.
 */
public record MensajeNotificacion(String destinatario, String asunto, String cuerpo) {

    public boolean tieneDestinatario() {
        return destinatario != null && !destinatario.isBlank();
    }
}
