package co.edu.uniquindio.epq.pqr.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.pqr.dominio.HistorialPqr;

public interface HistorialPqrRepository extends JpaRepository<HistorialPqr, Long> {

    @EntityGraph(attributePaths = "estado")
    List<HistorialPqr> findByPqrIdOrderByFechaCambioAscIdAsc(Long pqrId);
}
