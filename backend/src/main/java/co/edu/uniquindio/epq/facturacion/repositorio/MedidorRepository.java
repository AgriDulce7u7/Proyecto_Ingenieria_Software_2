package co.edu.uniquindio.epq.facturacion.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uniquindio.epq.facturacion.dominio.Medidor;

public interface MedidorRepository extends JpaRepository<Medidor, Integer> {

    Optional<Medidor> findFirstByContratoIdAndEstadoOrderByFechaInstalacionDesc(Integer contratoId, String estado);
}
