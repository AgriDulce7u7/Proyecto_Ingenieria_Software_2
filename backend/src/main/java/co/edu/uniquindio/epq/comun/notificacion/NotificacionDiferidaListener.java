package co.edu.uniquindio.epq.comun.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Patrón <b>Observer</b>: despacha las notificaciones después del commit. Si la operación de negocio se
 * revierte, no se notifica nada; si el envío falla, la operación de negocio no se afecta.
 */
@Component
public class NotificacionDiferidaListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionDiferidaListener.class);

    private final ServicioNotificacionSistema notificaciones;

    public NotificacionDiferidaListener(ServicioNotificacionSistema notificaciones) {
        this.notificaciones = notificaciones;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void despachar(NotificacionDiferidaEvento evento) {
        try {
            if (evento.rol() != null) {
                notificaciones.notificarRol(evento.rol(), evento.tipo(), evento.asunto(), evento.mensaje(),
                        evento.referencia());
            } else {
                notificaciones.notificarCorreo(evento.correo(), evento.tipo(), evento.asunto(), evento.mensaje(),
                        evento.referencia());
            }
        } catch (RuntimeException ex) {
            log.error("No fue posible enviar la notificación {} ({})", evento.tipo(), evento.referencia(), ex);
        }
    }
}
