package co.edu.uniquindio.epq.pagos.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.pagos.dominio.IntentoPagoEnLinea;

public interface IntentoPagoEnLineaRepository extends JpaRepository<IntentoPagoEnLinea, Long> {

    List<IntentoPagoEnLinea> findByFacturaIdOrderByFechaDesc(Long facturaId);
}
