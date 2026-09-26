package co.edu.uniquindio.epq.pqr.web.dto;

import java.time.LocalDateTime;

public record NotificacionResponse(
        Long id,
        String radicado,
        String tipo,
        String destinatario,
        String mensaje,
        LocalDateTime fechaEnvio,
        boolean enviado) {
}
