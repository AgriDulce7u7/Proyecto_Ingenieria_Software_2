package co.edu.uniquindio.epq.pqr.repositorio;

import java.time.LocalDateTime;
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

import co.edu.uniquindio.epq.pqr.dominio.Pqr;

public interface PqrRepository extends JpaRepository<Pqr, Long>, JpaSpecificationExecutor<Pqr> {

    @EntityGraph(attributePaths = {"ciudadano", "tipoSolicitud", "canal", "estado", "gestor"})
    Optional<Pqr> findByRadicado(String radicado);

    /** Último radicado del mes, usado para generar el consecutivo (SWR-05). */
    Optional<Pqr> findTopByRadicadoStartingWithOrderByRadicadoDesc(String prefijo);

    @Override
    @EntityGraph(attributePaths = {"ciudadano", "tipoSolicitud", "estado", "gestor"})
    List<Pqr> findAll(Specification<Pqr> especificacion, Sort orden);

    /** PQR cuyo plazo vence dentro de la ventana de alerta y aún no han sido notificadas (SWR-07). */
    @EntityGraph(attributePaths = {"estado", "gestor", "tipoSolicitud"})
    @Query("""
            SELECT p FROM Pqr p
            WHERE p.notificadoVencimiento = false
              AND p.estado.nombre IN :estados
              AND p.fechaLimiteRespuesta > :ahora
              AND p.fechaLimiteRespuesta <= :limite
            """)
    List<Pqr> buscarProximasAVencer(@Param("estados") Collection<String> estados,
                                    @Param("ahora") LocalDateTime ahora,
                                    @Param("limite") LocalDateTime limite);

    /** PQR cuyo plazo normativo ya venció sin respuesta (RN-04). */
    @EntityGraph(attributePaths = {"estado", "gestor"})
    @Query("""
            SELECT p FROM Pqr p
            WHERE p.estado.nombre IN :estados
              AND p.fechaLimiteRespuesta < :ahora
            """)
    List<Pqr> buscarConPlazoVencido(@Param("estados") Collection<String> estados,
                                    @Param("ahora") LocalDateTime ahora);
}
