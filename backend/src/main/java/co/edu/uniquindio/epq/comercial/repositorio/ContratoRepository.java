package co.edu.uniquindio.epq.comercial.repositorio;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comercial.dominio.EstadoContrato;

public interface ContratoRepository extends JpaRepository<Contrato, Integer>, JpaSpecificationExecutor<Contrato> {

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

    // ------------------------------------------------------------------ F-04 (entrega 2)

    @EntityGraph(attributePaths = {"cliente", "servicio", "predio", "predio.municipio", "tarifa"})
    Optional<Contrato> findByNumeroContrato(String numeroContrato);

    /** RN-01 / SWR-17: contratos vigentes (no cancelados) del mismo servicio en el mismo predio. */
    @Query("""
            select c from Contrato c
            where c.predio.id = :predioId and c.servicio.id = :servicioId and c.estado in :estados
            """)
    List<Contrato> buscarPorPredioServicioYEstados(@Param("predioId") Integer predioId,
                                                   @Param("servicioId") Integer servicioId,
                                                   @Param("estados") Collection<EstadoContrato> estados);

    @EntityGraph(attributePaths = {"servicio", "predio", "predio.municipio"})
    List<Contrato> findByClienteIdOrderByNumeroContratoAsc(Integer clienteId);

    @Override
    @EntityGraph(attributePaths = {"cliente", "servicio", "predio", "predio.municipio"})
    List<Contrato> findAll(Specification<Contrato> especificacion, Sort orden);
}
