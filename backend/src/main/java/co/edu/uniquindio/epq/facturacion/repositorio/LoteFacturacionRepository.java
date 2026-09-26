package co.edu.uniquindio.epq.facturacion.repositorio;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.facturacion.dominio.LoteFacturacion;

public interface LoteFacturacionRepository extends JpaRepository<LoteFacturacion, Integer> {

    Optional<LoteFacturacion> findByPeriodo(YearMonth periodo);

    List<LoteFacturacion> findAllByOrderByPeriodoDesc();
}
