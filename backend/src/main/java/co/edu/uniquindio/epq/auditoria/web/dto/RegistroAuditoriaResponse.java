package co.edu.uniquindio.epq.auditoria.web.dto;

import java.time.LocalDateTime;

import co.edu.uniquindio.epq.auditoria.dominio.RegistroAuditoria;

public record RegistroAuditoriaResponse(
        Long id,
        LocalDateTime fecha,
        String usuario,
        String rol,
        String modulo,
        String accion,
        String entidad,
        String entidadId,
        String detalle,
        boolean exitoso,
        String ipOrigen) {

    public static RegistroAuditoriaResponse de(RegistroAuditoria r) {
        return new RegistroAuditoriaResponse(r.getId(), r.getFecha(), r.getUsuario(), r.getRol(), r.getModulo().name(),
                r.getAccion().name(), r.getEntidad(), r.getEntidadId(), r.getDetalle(), r.isExitoso(), r.getIpOrigen());
    }
}
