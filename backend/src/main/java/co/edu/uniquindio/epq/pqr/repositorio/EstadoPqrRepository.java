package co.edu.uniquindio.epq.pqr.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.pqr.dominio.EstadoPqr;

public interface EstadoPqrRepository extends JpaRepository<EstadoPqr, Integer> {

    Optional<EstadoPqr> findByNombre(String nombre);

    List<EstadoPqr> findAllByOrderByOrdenAsc();
}
