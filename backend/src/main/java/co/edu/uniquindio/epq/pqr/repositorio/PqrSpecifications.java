package co.edu.uniquindio.epq.pqr.repositorio;

import org.springframework.data.jpa.domain.Specification;

import co.edu.uniquindio.epq.pqr.dominio.EstadoPqrTipo;
import co.edu.uniquindio.epq.pqr.dominio.Pqr;
import co.edu.uniquindio.epq.pqr.dominio.TipoSolicitudTipo;

/**
 * Patrón <b>Specification</b>: criterios de búsqueda de PQR reutilizables y combinables.
 * Agregar un nuevo filtro no obliga a crear nuevos métodos en el repositorio (OCP).
 */
public final class PqrSpecifications {

    private PqrSpecifications() {
    }

    public static Specification<Pqr> todas() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Pqr> conEstado(EstadoPqrTipo estado) {
        return (root, query, cb) -> cb.equal(root.get("estado").get("nombre"), estado.getNombre());
    }

    public static Specification<Pqr> conTipo(TipoSolicitudTipo tipo) {
        return (root, query, cb) -> cb.equal(root.get("tipoSolicitud").get("nombre"), tipo.getNombre());
    }

    public static Specification<Pqr> asignadaA(Integer gestorId) {
        return (root, query, cb) -> cb.equal(root.get("gestor").get("id"), gestorId);
    }

    public static Specification<Pqr> delCiudadano(String numeroDocumento) {
        return (root, query, cb) -> cb.equal(root.get("ciudadano").get("numeroDocumento"), numeroDocumento);
    }
}
