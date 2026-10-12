package co.edu.uniquindio.epq.comun.territorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MunicipioRepository extends JpaRepository<Municipio, Integer> {

    List<Municipio> findByActivoTrueOrderByNombreAsc();

    Optional<Municipio> findByNombreIgnoreCase(String nombre);
}
