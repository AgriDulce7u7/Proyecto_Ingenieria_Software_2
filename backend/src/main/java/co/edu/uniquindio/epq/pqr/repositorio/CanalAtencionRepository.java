package co.edu.uniquindio.epq.pqr.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.pqr.dominio.CanalAtencion;

public interface CanalAtencionRepository extends JpaRepository<CanalAtencion, Integer> {

    Optional<CanalAtencion> findByNombre(String nombre);
}
