package co.edu.uniquindio.epq.cartera.repositorio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.epq.cartera.dominio.AcuerdoPago;
import co.edu.uniquindio.epq.cartera.dominio.EstadoAcuerdo;
import co.edu.uniquindio.epq.cartera.dominio.EstadoCuota;

public interface AcuerdoPagoRepository extends JpaRepository<AcuerdoPago, Integer> {

    @EntityGraph(attributePaths = {"cuotas", "contrato", "contrato.cliente", "contrato.servicio"})
    Optional<AcuerdoPago> findByNumeroAcuerdo(String numeroAcuerdo);

    boolean existsByContratoIdAndEstado(Integer contratoId, EstadoAcuerdo estado);

    @EntityGraph(attributePaths = {"contrato", "contrato.cliente", "contrato.servicio"})
    List<AcuerdoPago> findAllByOrderByFechaAcuerdoDesc();

    /** SWR-31: acuerdos vigentes con alguna cuota pendiente ya vencida. */
    @EntityGraph(attributePaths = {"cuotas", "contrato", "contrato.cliente"})
    @Query("""
            select distinct a from AcuerdoPago a join a.cuotas c
            where a.estado = :vigente and c.estado = :pendiente and c.fechaVencimiento < :hoy
            """)
    List<AcuerdoPago> buscarConCuotaVencida(@Param("vigente") EstadoAcuerdo vigente,
                                            @Param("pendiente") EstadoCuota pendiente,
                                            @Param("hoy") LocalDate hoy);
}
