package co.edu.uniquindio.epq.comercial.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.comercial.dominio.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    Optional<Servicio> findByNombreIgnoreCase(String nombre);
}
