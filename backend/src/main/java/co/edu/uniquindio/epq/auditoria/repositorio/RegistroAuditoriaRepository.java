package co.edu.uniquindio.epq.auditoria.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.uniquindio.epq.auditoria.dominio.RegistroAuditoria;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long>,
        JpaSpecificationExecutor<RegistroAuditoria> {

    List<RegistroAuditoria> findTop50ByEntidadAndEntidadIdOrderByFechaDesc(String entidad, String entidadId);
}
