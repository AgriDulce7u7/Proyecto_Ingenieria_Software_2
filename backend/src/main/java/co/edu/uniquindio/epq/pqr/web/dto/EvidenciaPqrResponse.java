package co.edu.uniquindio.epq.pqr.web.dto;

import java.time.LocalDateTime;

/** Metadatos de una evidencia (sin el contenido del archivo). */
public record EvidenciaPqrResponse(Long id, String nombreArchivo, String tipoContenido, int tamanoBytes,
                                   String descripcion, LocalDateTime fechaCarga, String usuario) {
}
