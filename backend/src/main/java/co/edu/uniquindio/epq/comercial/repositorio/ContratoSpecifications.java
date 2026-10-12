package co.edu.uniquindio.epq.comercial.repositorio;

import org.springframework.data.jpa.domain.Specification;

import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comercial.dominio.EstadoContrato;
import co.edu.uniquindio.epq.comercial.dominio.TipoServicio;

/**
 * Patrón <b>Specification</b> para la búsqueda de contratos.
 */
public final class ContratoSpecifications {

    private ContratoSpecifications() {
    }

    public static Specification<Contrato> todos() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Contrato> conEstado(EstadoContrato estado) {
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<Contrato> deServicio(TipoServicio servicio) {
        return (root, query, cb) -> cb.equal(root.get("servicio").get("nombre"), servicio.getNombre());
    }

    public static Specification<Contrato> delUsuario(String codigoInterno) {
        return (root, query, cb) -> cb.equal(root.get("cliente").get("codigoInterno"), codigoInterno);
    }

    public static Specification<Contrato> delMunicipio(Integer municipioId) {
        return (root, query, cb) -> cb.equal(root.get("predio").get("municipio").get("id"), municipioId);
    }

    public static Specification<Contrato> candidatosSuspension() {
        return (root, query, cb) -> cb.isTrue(root.get("candidatoSuspension"));
    }
}
