package co.edu.uniquindio.epq.pqr.web.dto;

import java.time.LocalDateTime;

public record HistorialPqrResponse(Long id, String estado, LocalDateTime fechaCambio, String comentario, String usuario) {
}
