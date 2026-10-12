package co.edu.uniquindio.epq.comercial.web.dto;

import java.time.LocalDateTime;

public record HistorialContratoResponse(String estadoAnterior, String estadoNuevo, String motivo, String usuario,
                                        LocalDateTime fecha) {
}
