package co.edu.uniquindio.epq.pqr.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementación del prototipo: "envía" el correo escribiéndolo en el log de la aplicación.
 * Para producción se reemplaza por un adaptador SMTP (JavaMailSender) sin cambiar el resto del código.
 */
@Component
public class CanalCorreoSimulado implements CanalNotificacion {

    private static final Logger log = LoggerFactory.getLogger(CanalCorreoSimulado.class);

    @Override
    public boolean enviar(MensajeNotificacion mensaje) {
        if (!mensaje.tieneDestinatario()) {
            log.warn("[CORREO SIMULADO] Notificación sin destinatario: {}", mensaje.asunto());
            return false;
        }
        log.info("[CORREO SIMULADO] Para: {} | Asunto: {} | {}", mensaje.destinatario(), mensaje.asunto(),
                mensaje.cuerpo());
        return true;
    }
}
