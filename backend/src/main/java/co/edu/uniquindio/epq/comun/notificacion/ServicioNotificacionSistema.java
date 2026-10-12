package co.edu.uniquindio.epq.comun.notificacion;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Envía notificaciones por el canal configurado y deja trazabilidad en {@code notificacion_sistema}.
 *
 * <p>Cada envío se confirma en su propia transacción: un fallo del canal nunca revierte la operación
 * de negocio que originó el aviso.</p>
 */
@Service
public class ServicioNotificacionSistema {

    private static final Logger log = LoggerFactory.getLogger(ServicioNotificacionSistema.class);

    private final NotificacionSistemaRepository repositorio;
    private final CanalNotificacion canal;
    private final DirectorioFuncionarios directorio;
    private final Clock clock;

    public ServicioNotificacionSistema(NotificacionSistemaRepository repositorio, CanalNotificacion canal,
                                       DirectorioFuncionarios directorio, Clock clock) {
        this.repositorio = repositorio;
        this.canal = canal;
        this.directorio = directorio;
        this.clock = clock;
    }

    /** Notificación a un usuario externo (ciudadano) identificado por su correo. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notificarCorreo(String correo, TipoNotificacionSistema tipo, String asunto, String mensaje,
                                String referencia) {
        enviarYRegistrar(correo, null, tipo, asunto, mensaje, referencia);
    }

    /** Notificación a todos los funcionarios activos de un rol (p. ej. GESTOR_CARTERA). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int notificarRol(String rol, TipoNotificacionSistema tipo, String asunto, String mensaje, String referencia) {
        List<DestinatarioInterno> destinatarios = directorio.activosConRol(rol);
        destinatarios.forEach(d -> enviarYRegistrar(d.correo(), d.funcionarioId(), tipo, asunto, mensaje, referencia));
        return destinatarios.size();
    }

    private void enviarYRegistrar(String correo, Integer funcionarioId, TipoNotificacionSistema tipo, String asunto,
                                  String mensaje, String referencia) {
        boolean enviado;
        try {
            enviado = canal.enviar(new MensajeNotificacion(correo, asunto, mensaje));
        } catch (RuntimeException ex) {
            log.error("Falló el envío de la notificación {} ({})", tipo, referencia, ex);
            enviado = false;
        }
        repositorio.save(NotificacionSistema.registrar(correo, funcionarioId, tipo.name(), asunto, mensaje, referencia,
                LocalDateTime.now(clock), enviado));
    }
}
