package co.edu.uniquindio.epq.cartera.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.cartera.dominio.EstadoProcesoCobro;
import co.edu.uniquindio.epq.cartera.dominio.ProcesoCobro;

public interface ProcesoCobroRepository extends JpaRepository<ProcesoCobro, Integer> {

    Optional<ProcesoCobro> findByContratoId(Integer contratoId);

    @EntityGraph(attributePaths = {"contrato", "contrato.cliente", "contrato.servicio"})
    List<ProcesoCobro> findByEstadoOrderByFechaInicioAsc(EstadoProcesoCobro estado);

    @EntityGraph(attributePaths = {"contrato", "contrato.cliente", "contrato.servicio"})
    List<ProcesoCobro> findAllByOrderByFechaActualizacionDesc();
}
