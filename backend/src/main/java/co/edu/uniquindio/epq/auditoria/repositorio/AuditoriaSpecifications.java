package co.edu.uniquindio.epq.auditoria.repositorio;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import co.edu.uniquindio.epq.auditoria.dominio.AccionAuditoria;
import co.edu.uniquindio.epq.auditoria.dominio.ModuloAuditoria;
import co.edu.uniquindio.epq.auditoria.dominio.RegistroAuditoria;

/**
 * Patrón <b>Specification</b> para filtrar el log de auditoría (CU-11).
 */
public final class AuditoriaSpecifications {

    private AuditoriaSpecifications() {
    }

    public static Specification<RegistroAuditoria> todos() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<RegistroAuditoria> delUsuario(String usuario) {
        return (root, query, cb) -> cb.equal(cb.lower(root.get("usuario")), usuario.trim().toLowerCase());
    }

    public static Specification<RegistroAuditoria> delModulo(ModuloAuditoria modulo) {
        return (root, query, cb) -> cb.equal(root.get("modulo"), modulo);
    }

    public static Specification<RegistroAuditoria> conAccion(AccionAuditoria accion) {
        return (root, query, cb) -> cb.equal(root.get("accion"), accion);
    }

    public static Specification<RegistroAuditoria> deEntidad(String entidad, String entidadId) {
        return (root, query, cb) -> entidadId == null
                ? cb.equal(root.get("entidad"), entidad)
                : cb.and(cb.equal(root.get("entidad"), entidad), cb.equal(root.get("entidadId"), entidadId));
    }

    public static Specification<RegistroAuditoria> desde(LocalDateTime desde) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), desde);
    }

    public static Specification<RegistroAuditoria> hasta(LocalDateTime hasta) {
        return (root, query, cb) -> cb.lessThan(root.get("fecha"), hasta);
    }

    public static Specification<RegistroAuditoria> soloFallidos() {
        return (root, query, cb) -> cb.isFalse(root.get("exitoso"));
    }
}
