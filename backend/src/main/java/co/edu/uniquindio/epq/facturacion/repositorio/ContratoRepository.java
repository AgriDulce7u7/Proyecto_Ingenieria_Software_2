package co.edu.uniquindio.epq.facturacion.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.epq.facturacion.dominio.Contrato;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoContrato;

public interface ContratoRepository extends JpaRepository<Contrato, Integer> {

    /** Solo se cargan los identificadores: cada contrato se procesa en su propia transacción. */
    @Query("select c.id from Contrato c where c.estado = :estado order by c.id")
    List<Integer> buscarIdsPorEstado(@Param("estado") EstadoContrato estado);

    @EntityGraph(attributePaths = {"cliente", "servicio"})
    @Query("select c from Contrato c order by c.numeroContrato")
    List<Contrato> buscarTodosConDetalle();

    @EntityGraph(attributePaths = {"cliente", "servicio"})
    @Query("select c from Contrato c where c.estado = :estado order by c.numeroContrato")
    List<Contrato> buscarPorEstadoConDetalle(@Param("estado") EstadoContrato estado);

    @EntityGraph(attributePaths = {"cliente", "servicio"})
    Optional<Contrato> findWithDetalleById(Integer id);

    long countByEstado(EstadoContrato estado);
}
