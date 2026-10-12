package co.edu.uniquindio.epq.comun.notificacion;

/**
 * Solicitud de notificación que se envía solo si la transacción que la originó se confirma.
 * Indique {@code correo} (usuario externo) o {@code rol} (todos los funcionarios activos de ese rol).
 */
public record NotificacionDiferidaEvento(String correo, String rol, TipoNotificacionSistema tipo, String asunto,
                                         String mensaje, String referencia) {

    public static NotificacionDiferidaEvento aCorreo(String correo, TipoNotificacionSistema tipo, String asunto,
                                                     String mensaje, String referencia) {
        return new NotificacionDiferidaEvento(correo, null, tipo, asunto, mensaje, referencia);
    }

    public static NotificacionDiferidaEvento aRol(String rol, TipoNotificacionSistema tipo, String asunto,
                                                  String mensaje, String referencia) {
        return new NotificacionDiferidaEvento(null, rol, tipo, asunto, mensaje, referencia);
    }
}
