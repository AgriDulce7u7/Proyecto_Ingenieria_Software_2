package co.edu.uniquindio.epq.pqr.repositorio;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.epq.pqr.dominio.Gestor;

public interface GestorRepository extends JpaRepository<Gestor, Integer> {

    List<Gestor> findByActivoTrueOrderByNombreAsc();

    /**
     * Gestor activo con menos PQR pendientes de respuesta (desempate por id).
     */
    @Query(value = """
            SELECT g.* FROM gestor g
            LEFT JOIN pqr p ON p.gestor_id = g.id
                 AND p.estado_id IN (SELECT e.id FROM estado_pqr e WHERE e.nombre IN (:estados))
            WHERE g.activo = TRUE
            GROUP BY g.id
            ORDER BY COUNT(p.id) ASC, g.id ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Gestor> buscarGestorConMenorCarga(@Param("estados") Collection<String> estadosPendientes);
}
