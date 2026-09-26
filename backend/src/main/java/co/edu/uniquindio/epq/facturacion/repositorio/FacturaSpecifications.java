package co.edu.uniquindio.epq.facturacion.repositorio;

import java.time.YearMonth;

import org.springframework.data.jpa.domain.Specification;

import co.edu.uniquindio.epq.facturacion.dominio.EstadoFactura;
import co.edu.uniquindio.epq.facturacion.dominio.Factura;

/**
 * Patrón <b>Specification</b>: filtros combinables para la consulta de facturas.
 */
public final class FacturaSpecifications {

    private FacturaSpecifications() {
    }

    public static Specification<Factura> todas() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Factura> delPeriodo(YearMonth periodo) {
        return (root, query, cb) -> cb.equal(root.get("periodo"), periodo);
    }

    public static Specification<Factura> delContrato(String numeroContrato) {
        return (root, query, cb) -> cb.equal(root.get("contrato").get("numeroContrato"), numeroContrato);
    }

    public static Specification<Factura> conEstado(EstadoFactura estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<Factura> delLote(Integer loteId) {
        return (root, query, cb) -> cb.equal(root.get("lote").get("id"), loteId);
    }

    public static Specification<Factura> delCliente(String numeroDocumento) {
        return (root, query, cb) ->
                cb.equal(root.get("contrato").get("cliente").get("numeroDocumento"), numeroDocumento);
    }
}
