package co.edu.uniquindio.epq.comun.notificacion;

/**
 * Mensaje a enviar por un canal de notificación.
 */
public record MensajeNotificacion(String destinatario, String asunto, String cuerpo) {

    public boolean tieneDestinatario() {
        return destinatario != null && !destinatario.isBlank();
    }
}
