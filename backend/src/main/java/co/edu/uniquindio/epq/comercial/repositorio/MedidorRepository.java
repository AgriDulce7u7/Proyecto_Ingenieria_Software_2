package co.edu.uniquindio.epq.comercial.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.comercial.dominio.Medidor;

public interface MedidorRepository extends JpaRepository<Medidor, Integer> {

    Optional<Medidor> findFirstByContratoIdAndEstadoOrderByFechaInstalacionDesc(Integer contratoId, String estado);

    boolean existsByNumeroSerieIgnoreCase(String numeroSerie);

    List<Medidor> findByContratoIdOrderByFechaInstalacionDesc(Integer contratoId);
}
