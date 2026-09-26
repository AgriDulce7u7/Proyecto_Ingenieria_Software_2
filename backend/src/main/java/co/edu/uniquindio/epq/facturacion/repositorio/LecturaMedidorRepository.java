package co.edu.uniquindio.epq.facturacion.repositorio;

import java.time.YearMonth;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.facturacion.dominio.LecturaMedidor;

public interface LecturaMedidorRepository extends JpaRepository<LecturaMedidor, Long> {

    Optional<LecturaMedidor> findByMedidorIdAndPeriodo(Integer medidorId, YearMonth periodo);
}
