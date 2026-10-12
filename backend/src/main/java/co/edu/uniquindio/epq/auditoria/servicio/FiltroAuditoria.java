package co.edu.uniquindio.epq.auditoria.servicio;

import java.time.LocalDate;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.dominio.ModuloAuditoria;

/**
 * Criterios de consulta del log de auditoría (CU-11).
 */
public record FiltroAuditoria(String usuario, ModuloAuditoria modulo, AccionAuditoria accion, String entidad,
                              String entidadId, LocalDate desde, LocalDate hasta, boolean soloFallidos) {
}
