package co.edu.uniquindio.epq.pqr.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uniquindio.epq.pqr.dominio.EvidenciaPqr;
import co.edu.uniquindio.epq.pqr.web.dto.EvidenciaPqrResponse;

public interface EvidenciaPqrRepository extends JpaRepository<EvidenciaPqr, Long> {

    long countByPqrId(Long pqrId);

    /** Proyección: lista las evidencias sin traer el contenido binario. */
    @Query("""
            select new co.edu.uniquindio.epq.pqr.web.dto.EvidenciaPqrResponse(
                e.id, e.nombreArchivo, e.tipoContenido, e.tamanoBytes, e.descripcion, e.fechaCarga, e.usuario)
            from EvidenciaPqr e where e.pqr.id = :pqrId order by e.fechaCarga
            """)
    List<EvidenciaPqrResponse> listarMetadatos(@Param("pqrId") Long pqrId);

    Optional<EvidenciaPqr> findByIdAndPqrId(Long id, Long pqrId);
}
