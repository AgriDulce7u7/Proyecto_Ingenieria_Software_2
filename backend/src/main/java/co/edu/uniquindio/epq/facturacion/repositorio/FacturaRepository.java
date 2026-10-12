package co.edu.uniquindio.epq.facturacion.repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.epq.facturacion.dominio.EstadoCobroFactura;
import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;
import jakarta.persistence.LockModeType;

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

    // ------------------------------------------------------------------ entrega 2: cobro, portal y cartera

    /** Bloqueo pesimista: evita aplicar dos pagos simultáneos a la misma factura (SWR-23). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Factura f where f.numeroFactura = :numero")
    Optional<Factura> buscarParaActualizar(@Param("numero") String numeroFactura);

    /** DE-01 vencerPlazo(): facturas pendientes, fuera de acuerdo, cuya fecha de vencimiento ya pasó. */
    @Query("""
            select f from Factura f
            where f.estadoCobro = :pendiente and f.acuerdoPagoId is null and f.fechaVencimiento < :hoy
            """)
    List<Factura> buscarPendientesVencidas(@Param("pendiente") EstadoCobroFactura pendiente,
                                           @Param("hoy") LocalDate hoy);

    @EntityGraph(attributePaths = {"lectura", "tarifa"})
    List<Factura> findByContratoIdOrderByPeriodoDesc(Integer contratoId);

    @EntityGraph(attributePaths = {"contrato", "contrato.cliente", "contrato.servicio", "lectura",
            "lectura.medidor", "tarifa", "lote"})
    Optional<Factura> findByContratoIdAndPeriodo(Integer contratoId, YearMonth periodo);

    List<Factura> findByContratoIdAndEstadoCobroOrderByPeriodoAsc(Integer contratoId, EstadoCobroFactura estadoCobro);

    List<Factura> findByAcuerdoPagoIdOrderByPeriodoAsc(Integer acuerdoPagoId);

    long countByLoteIdAndEstado(Integer loteId, EstadoFactura estado);

    @Query("select coalesce(sum(f.total), 0) from Factura f where f.lote.id = :loteId")
    BigDecimal sumarTotalPorLote(@Param("loteId") Integer loteId);
}
