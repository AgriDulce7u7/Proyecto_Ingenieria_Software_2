package co.edu.uniquindio.epq.pqr.evento;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.notificacion.ServicioNotificacionPqr;

/**
 * Patrón <b>Observer</b>: envía notificaciones al ciudadano y al gestor cuando ocurren eventos.
 *
 * <p>Se ejecuta DESPUÉS del commit; {@link ServicioNotificacionPqr} abre una transacción nueva.
 * Un fallo en el envío de correos nunca revierte el registro de la PQR
 * (CO-01: "la PQR se crea igualmente").</p>
 */
@Component
public class NotificacionPqrListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionPqrListener.class);

    private final ServicioNotificacionPqr notificaciones;

    public NotificacionPqrListener(ServicioNotificacionPqr notificaciones) {
        this.notificaciones = notificaciones;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alRegistrar(PqrRegistradaEvento evento) {
        ejecutarSeguro(() -> notificaciones.notificarRadicacion(evento.pqrId()), evento);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alAsignar(PqrAsignadaEvento evento) {
        ejecutarSeguro(() -> notificaciones.notificarAsignacion(evento.pqrId()), evento);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiarEstado(PqrEstadoCambiadoEvento evento) {
        if (evento.estadoNuevo() == EstadoPqrTipo.RESUELTO) {
            ejecutarSeguro(() -> notificaciones.notificarRespuesta(evento.pqrId()), evento);
        } else if (evento.estadoNuevo() == EstadoPqrTipo.VENCIDO) {
            ejecutarSeguro(() -> notificaciones.notificarVencimiento(evento.pqrId()), evento);
        }
    }

    private void ejecutarSeguro(Runnable accion, Object evento) {
        try {
            accion.run();
        } catch (RuntimeException ex) {
            log.error("No fue posible procesar la notificación del evento {}", evento, ex);
        }
    }
}
