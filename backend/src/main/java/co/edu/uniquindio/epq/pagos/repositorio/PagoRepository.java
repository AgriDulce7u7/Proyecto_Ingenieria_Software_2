package co.edu.uniquindio.epq.pagos.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.uniquindio.epq.pagos.dominio.Pago;

public interface PagoRepository extends JpaRepository<Pago, Long>, JpaSpecificationExecutor<Pago> {

    @EntityGraph(attributePaths = {"factura", "factura.contrato", "factura.contrato.cliente", "factura.contrato.servicio"})
    Optional<Pago> findByCodigoPago(String codigoPago);

    @EntityGraph(attributePaths = {"factura", "factura.contrato", "factura.contrato.cliente", "factura.contrato.servicio"})
    Optional<Pago> findWithDetalleById(Long id);

    Optional<Pago> findByFacturaId(Long facturaId);

    @Override
    @EntityGraph(attributePaths = {"factura", "factura.contrato", "factura.contrato.cliente", "factura.contrato.servicio"})
    List<Pago> findAll(Specification<Pago> especificacion, Sort orden);
}
