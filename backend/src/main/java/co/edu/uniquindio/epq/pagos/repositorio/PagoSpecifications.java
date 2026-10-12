package co.edu.uniquindio.epq.pagos.repositorio;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import co.edu.uniquindio.epq.pagos.dominio.CanalPago;
import co.edu.uniquindio.epq.pagos.dominio.Pago;

/** Patrón <b>Specification</b> para la consulta de pagos. */
public final class PagoSpecifications {

    private PagoSpecifications() {
    }

    public static Specification<Pago> todos() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Pago> deFactura(String numeroFactura) {
        return (root, query, cb) -> cb.equal(root.get("factura").get("numeroFactura"), numeroFactura);
    }

    public static Specification<Pago> deContrato(String numeroContrato) {
        return (root, query, cb) -> cb.equal(root.get("factura").get("contrato").get("numeroContrato"), numeroContrato);
    }

    public static Specification<Pago> porCanal(CanalPago canal) {
        return (root, query, cb) -> cb.equal(root.get("canal"), canal);
    }

    public static Specification<Pago> desde(LocalDateTime desde) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaPago"), desde);
    }

    public static Specification<Pago> hasta(LocalDateTime hasta) {
        return (root, query, cb) -> cb.lessThan(root.get("fechaPago"), hasta);
    }
}
