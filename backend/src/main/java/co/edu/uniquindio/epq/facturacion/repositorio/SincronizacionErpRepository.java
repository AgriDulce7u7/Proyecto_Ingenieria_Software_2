package co.edu.uniquindio.epq.facturacion.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.facturacion.dominio.SincronizacionErp;

public interface SincronizacionErpRepository extends JpaRepository<SincronizacionErp, Long> {

    List<SincronizacionErp> findByFacturaIdOrderByFechaSincronizacionDesc(Long facturaId);
}
