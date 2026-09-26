package co.edu.uniquindio.epq.facturacion.repositorio;

import java.math.BigDecimal;
import java.time.YearMonth;
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

import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;

public interface FacturaRepository extends JpaRepository<Factura, Long>, JpaSpecificationExecutor<Factura> {

    /** Garantiza la idempotencia: un contrato solo tiene una factura por periodo. */
    boolean existsByContratoIdAndPeriodo(Integer contratoId, YearMonth periodo);

    @EntityGraph(attributePaths = {"contrato", "contrato.cliente", "contrato.servicio", "lectura",
            "lectura.medidor", "tarifa", "lote"})
    Optional<Factura> findByNumeroFactura(String numeroFactura);

    @EntityGraph(attributePaths = {"contrato", "contrato.cliente", "contrato.servicio", "lectura",
            "lectura.medidor", "tarifa", "lote"})
    Optional<Factura> findWithDetalleById(Long id);

    @Query("select f.id from Factura f where f.lote.id = :loteId and f.estado in :estados order by f.id")
    List<Long> buscarIdsPorLoteYEstados(@Param("loteId") Integer loteId,
                                        @Param("estados") Collection<EstadoFactura> estados);

    /** Búsqueda con filtros (Specification) cargando en una sola consulta los datos a mostrar. */
    @Override
    @EntityGraph(attributePaths = {"contrato", "contrato.cliente", "contrato.servicio"})
    List<Factura> findAll(Specification<Factura> spec, Sort sort);

    long countByLoteId(Integer loteId);

    long countByLoteIdAndEstado(Integer loteId, EstadoFactura estado);

    @Query("select coalesce(sum(f.total), 0) from Factura f where f.lote.id = :loteId")
    BigDecimal sumarTotalPorLote(@Param("loteId") Integer loteId);
}
