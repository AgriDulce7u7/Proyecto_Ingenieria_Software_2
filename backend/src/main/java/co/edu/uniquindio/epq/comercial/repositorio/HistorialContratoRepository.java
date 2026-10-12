package co.edu.uniquindio.epq.comercial.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.comercial.dominio.HistorialContrato;

public interface HistorialContratoRepository extends JpaRepository<HistorialContrato, Long> {

    List<HistorialContrato> findByContratoIdOrderByFechaAscIdAsc(Integer contratoId);
}
