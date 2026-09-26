package co.edu.uniquindio.epq.pqr.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.pqr.dominio.Notificacion;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    @EntityGraph(attributePaths = {"pqr", "gestor"})
    List<Notificacion> findByPqrIdOrderByFechaEnvioDescIdDesc(Long pqrId);

    @EntityGraph(attributePaths = {"pqr", "gestor"})
    List<Notificacion> findByGestorIdOrderByFechaEnvioDescIdDesc(Integer gestorId);
}
