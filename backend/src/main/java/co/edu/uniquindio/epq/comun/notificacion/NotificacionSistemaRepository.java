package co.edu.uniquindio.epq.comun.notificacion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionSistemaRepository extends JpaRepository<NotificacionSistema, Long> {

    List<NotificacionSistema> findTop100ByFuncionarioIdOrderByFechaEnvioDesc(Integer funcionarioId);

    List<NotificacionSistema> findByReferenciaOrderByFechaEnvioDesc(String referencia);

    long countByFuncionarioIdAndLeidaFalse(Integer funcionarioId);
}
