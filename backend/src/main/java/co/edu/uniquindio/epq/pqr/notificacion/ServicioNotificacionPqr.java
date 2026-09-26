package co.edu.uniquindio.epq.pqr.notificacion;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniquindio.epq.comun.excepcion.RecursoNoEncontradoException;
import co.edu.uniquindio.epq.pqr.dominio.Gestor;
import co.edu.uniquindio.epq.pqr.dominio.Notificacion;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.dominio.TipoNotificacion;
import co.edu.uniquindio.epq.pqr.repositorio.NotificacionRepository;
import co.edu.uniquindio.epq.pqr.repositorio.PqrRepository;

/**
 * Redacta, envía y registra en la tabla {@code notificacion} los avisos del ciclo de vida de una PQR.
 * Cada envío queda trazado con su resultado ({@code enviado_ok}).
 */
@Service
public class ServicioNotificacionPqr {

    private static final Logger log = LoggerFactory.getLogger(ServicioNotificacionPqr.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PqrRepository pqrRepository;
    private final NotificacionRepository notificacionRepository;
    private final CanalNotificacion canal;
    private final Clock clock;

    public ServicioNotificacionPqr(PqrRepository pqrRepository, NotificacionRepository notificacionRepository,
                                   CanalNotificacion canal, Clock clock) {
        this.pqrRepository = pqrRepository;
        this.notificacionRepository = notificacionRepository;
        this.canal = canal;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notificarRadicacion(Long pqrId) {
        Pqr pqr = buscar(pqrId);
        String cuerpo = "Estimado(a) %s, su %s fue radicada con el número %s. Fecha estimada de respuesta: %s. "
                .formatted(pqr.getCiudadano().getNombreCompleto(), pqr.getTipoSolicitud().getNombre().toLowerCase(),
                        pqr.getRadicado(), pqr.getFechaEstimadaRespuesta().format(FORMATO_FECHA))
                + "Puede consultar el estado en el portal de EPQ con su número de radicado.";
        enviarYRegistrar(pqr, null, TipoNotificacion.RADICACION_CIUDADANO, pqr.getCiudadano().getCorreo(),
                "EPQ - PQR radicada " + pqr.getRadicado(), cuerpo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notificarAsignacion(Long pqrId) {
        Pqr pqr = buscar(pqrId);
        Gestor gestor = pqr.getGestor();
        if (gestor == null) {
            return;
        }
        String cuerpo = "Se le asignó la PQR %s (%s): \"%s\". Fecha límite de respuesta: %s."
                .formatted(pqr.getRadicado(), pqr.getTipoSolicitud().getNombre(), pqr.getAsunto(),
                        pqr.getFechaLimiteRespuesta().format(FORMATO_FECHA_HORA));
        enviarYRegistrar(pqr, gestor, TipoNotificacion.ASIGNACION_GESTOR, gestor.getCorreo(),
                "SIGCA-EPQ - Nueva PQR asignada " + pqr.getRadicado(), cuerpo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notificarRespuesta(Long pqrId) {
        Pqr pqr = buscar(pqrId);
        String cuerpo = "Estimado(a) %s, su PQR %s fue respondida: %s"
                .formatted(pqr.getCiudadano().getNombreCompleto(), pqr.getRadicado(), pqr.getRespuesta());
        enviarYRegistrar(pqr, null, TipoNotificacion.RESPUESTA_CIUDADANO, pqr.getCiudadano().getCorreo(),
                "EPQ - Respuesta a su PQR " + pqr.getRadicado(), cuerpo);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notificarVencimiento(Long pqrId) {
        Pqr pqr = buscar(pqrId);
        Gestor gestor = pqr.getGestor();
        String cuerpo = "La PQR %s venció el %s sin respuesta formal. El caso queda registrado para el reporte a la SSPD."
                .formatted(pqr.getRadicado(), pqr.getFechaLimiteRespuesta().format(FORMATO_FECHA_HORA));
        enviarYRegistrar(pqr, gestor, TipoNotificacion.PQR_VENCIDA, gestor != null ? gestor.getCorreo() : null,
                "SIGCA-EPQ - PQR VENCIDA " + pqr.getRadicado(), cuerpo);
    }

    /** Alerta al gestor cuando faltan pocas horas para el vencimiento (SWR-07). */
    @Transactional
    public void notificarAlertaVencimiento(Pqr pqr, LocalDateTime ahora) {
        Gestor gestor = pqr.getGestor();
        long horasRestantes = Math.max(0, Duration.between(ahora, pqr.getFechaLimiteRespuesta()).toHours());
        String cuerpo = "ALERTA: a la PQR %s le quedan aproximadamente %d horas para vencer (límite: %s)."
                .formatted(pqr.getRadicado(), horasRestantes, pqr.getFechaLimiteRespuesta().format(FORMATO_FECHA_HORA));
        enviarYRegistrar(pqr, gestor, TipoNotificacion.VENCIMIENTO_48H, gestor != null ? gestor.getCorreo() : null,
                "SIGCA-EPQ - PQR próxima a vencer " + pqr.getRadicado(), cuerpo);
    }

    private void enviarYRegistrar(Pqr pqr, Gestor gestor, TipoNotificacion tipo, String destinatario,
                                  String asunto, String cuerpo) {
        boolean enviado;
        try {
            enviado = canal.enviar(new MensajeNotificacion(destinatario, asunto, cuerpo));
        } catch (RuntimeException ex) {
            log.error("Fallo el envío de la notificación {} de la PQR {}", tipo, pqr.getRadicado(), ex);
            enviado = false;
        }
        String mensaje = "%s | Para: %s | %s".formatted(asunto, destinatario == null ? "(sin correo)" : destinatario, cuerpo);
        notificacionRepository.save(Notificacion.registrar(pqr, gestor, tipo, mensaje, LocalDateTime.now(clock), enviado));
    }

    private Pqr buscar(Long pqrId) {
        return pqrRepository.findById(pqrId).orElseThrow(() -> new RecursoNoEncontradoException("PQR", pqrId));
    }
}
