package co.edu.uniquindio.epq.pqr.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.pqr.dominio.Ciudadano;

public interface CiudadanoRepository extends JpaRepository<Ciudadano, Integer> {

    Optional<Ciudadano> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);
}
