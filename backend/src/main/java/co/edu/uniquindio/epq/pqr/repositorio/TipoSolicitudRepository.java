package co.edu.uniquindio.epq.pqr.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitud;

public interface TipoSolicitudRepository extends JpaRepository<TipoSolicitud, Integer> {

    Optional<TipoSolicitud> findByNombre(String nombre);
}
